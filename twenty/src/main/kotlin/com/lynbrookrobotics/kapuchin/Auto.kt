package com.lynbrookrobotics.kapuchin

import com.lynbrookrobotics.kapuchin.choreos.*
import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.control.math.drivetrain.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.subsystems.shooter.*
import info.kunalsheth.units.generated.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import java.io.File

suspend fun Subsystems.`shoot wall`() = genericAuto(
        fastAsFuckLine(6.Foot),
        reverse = false, collect = false, rezero = true, shootTimeout = 10.Second
)

suspend fun Subsystems.`I1 shoot C1`() {
    val trajI1C1 = mediumAsFuck("I1C1")
    if (trajI1C1 == null) {
        log(Error) { "Path I1C1 doesn't exist, running shoot wall" }
        `shoot wall`()
    } else genericAuto(trajI1C1, reverse = true, collect = true, rezero = true, shootTimeout = 10.Second)
}

suspend fun Subsystems.`I2 shoot C1`() {
    val trajI2C1 = mediumAsFuck("I2C1")
    if (trajI2C1 == null) {
        log(Error) { "Path I2C1 doesn't exist, running shoot wall" }
        `shoot wall`()
    } else genericAuto(trajI2C1, reverse = true, collect = true, rezero = true, shootTimeout = 10.Second)
}

suspend fun Subsystems.`I3 shoot C5`() {
    val trajI3C5 = mediumAsFuck("I3C5")
    if (trajI3C5 == null) {
        log(Error) { "Path I3C5 doesn't exist, running shoot wall" }
        `shoot wall`()
    } else genericAuto(trajI3C5, reverse = false, collect = true, rezero = true, shootTimeout = 10.Second)
}

suspend fun Subsystems.`I1 shoot C1 I2 shoot`() {
    val trajC1I2 = fastAsFuck("C1I2") ?: fastAsFuckLine(8.Foot).also {
        log(Error) { "Path C1I2 doesn't exist, fallbacking to 8 foot line" }
    }

    `I1 shoot C1`()
    drivetrain.followTrajectory(trajC1I2, 15.Inch, 5.Inch, reverse = false)
    genericAuto(null, reverse = false, collect = false, rezero = false)
}

suspend fun Subsystems.`I3 shoot C5 I3 shoot`() {
    val pathC5I3 = fastAsFuck("C5I3")
    if (pathC5I3 == null) log(Error) { "C5I3 path doesn't exist!!" }
    `I3 shoot C5`()
    pathC5I3?.let { drivetrain.followTrajectory(it, 15.Inch, 5.Inch, reverse = false) }
    genericAuto(null, reverse = false, collect = false, rezero = false)
}

private fun loadPath(name: String) = Thread.currentThread()
        .contextClassLoader
        .getResourceAsStream("com/lynbrookrobotics/kapuchin/paths/$name.tsv")
        ?.bufferedReader()
        ?.lineSequence()
        ?.drop(1)
        ?.map { it.split('\t') }
        ?.map { it.map { tkn -> tkn.trim() } }
        ?.map { Waypoint(it[0].toDouble().Foot, it[1].toDouble().Foot) }
        ?.toList()

private fun loadTempPath() = File("/home/lvuser/journal.tsv")
        .bufferedReader()
        .lineSequence()
        .drop(1)
        .map { it.split('\t') }
        .map { it.map { tkn -> tkn.trim() } }
        .map { Waypoint(it[0].toDouble().Foot, it[1].toDouble().Foot) }
        .toList()

private fun Subsystems.fastAsFuckLine(dist: Length): Trajectory =
        pathToTrajectory(
                nSect(Waypoint(0.Foot, 0.Foot), Waypoint(0.Foot, dist), 3.Inch),
                drivetrain.maxSpeed, drivetrain.percentMaxOmega * drivetrain.maxOmega, drivetrain.maxAcceleration
        )

/**
 * Trajectory of the named path at max limits, null if path TSV doesn't exist.
 */
private fun Subsystems.fastAsFuck(name: String): Trajectory? = loadPath(name)?.let {
    pathToTrajectory(it, drivetrain.maxSpeed, drivetrain.percentMaxOmega * drivetrain.maxOmega, drivetrain.maxAcceleration)
}

/**
 * Trajectory of the named path at max limits but half max speed, null if path TSV doesn't exist.
 * Use when intaking balls while driving.
 */
private fun Subsystems.mediumAsFuck(name: String): Trajectory? = loadPath(name)?.let {
    pathToTrajectory(it, drivetrain.maxSpeed / 2, drivetrain.percentMaxOmega * drivetrain.maxOmega / 2, drivetrain.maxAcceleration)
}

/**
 * A generic auto that shoots, drives, and collects.
 *
 * @param endingTrajectory the trajectory to follow AFTER shooting balls.
 * @param reverse whether or not to follow trajectory in reverse (reverse = intake on front).
 * @param collect whether or not to collect balls while following the trajectory.
 * @param rezero whether or not to rezero the turret and reindex the carousel while following the trajectory.
 * @param shootTimeout timeout on shooting before rezering and following trajectory.
 */
private suspend fun Subsystems.genericAuto(
        endingTrajectory: Trajectory?,
        reverse: Boolean,
        collect: Boolean,
        rezero: Boolean,
        shootTimeout: Time = 15.Second
) {
    if (flywheel == null || feederRoller == null) {
        log(Error) { "Need flywheel and feeder to run auto" }
        freeze()
    } else startChoreo("Generic Auto") {

        val reading by limelight.hardware.readings.readEagerly().withoutStamps
        val robotPosition by drivetrain.hardware.position.readEagerly().withoutStamps
        val flywheelSpeed by flywheel.hardware.speed.readEagerly().withoutStamps
        val feederSpeed by feederRoller.hardware.speed.readEagerly().withoutStamps

        suspend fun finish() = supervisorScope {
            val carouselJob = launch { if (rezero) carousel.whereAreMyBalls() }
            val turretJob = launch { if (rezero) turret?.rezero(electrical) }

            if (collect) {
                log(Debug) { "Waiting for carousel to rezero before collection" }
                carouselJob.join()
            }
            val collectJob = launch { if (collect) eat() }
            endingTrajectory?.let { drivetrain.followTrajectory(it, 12.Inch, 2.Inch, reverse) }

            collectJob.cancel()
            turretJob.cancel()
        }

        choreography {
            carousel.rezero()

            withTimeout(shootTimeout) {
                val reading1 = reading
                if (reading1?.pipeline == null) {
                    log(Warning) { "Limelight pipeline is null!! finishing early" }
                    finish()
                    throw CancellationException()
                }

                launch { limelight.set(reading1.pipeline) }

                val snapshot1 = bestShot(limelight.hardware.conversions.goalPositions(reading1, robotPosition.bearing))
                if (snapshot1 == null) {
                    log(Warning) { "Couldn't find snapshot1 or no shots possible, finishing early" }
                    finish()
                    throw CancellationException()
                }

                withTimeout(2.Second) { turret?.trackTarget(limelight, flywheel, drivetrain, snapshot1.goal, 1.Degree) }
                launch { turret?.trackTarget(limelight, flywheel, drivetrain, snapshot1.goal) }
                withTimeout(1.Second) { limelight.autoZoom() }

                val snapshot2 = reading?.let { bestShot(limelight.hardware.conversions.goalPositions(it, robotPosition.bearing)) }
                if (snapshot2 == null) {
                    log(Warning) { "Couldn't find snapshot2 or no shots possible, finishing early" }
                    finish()
                    throw CancellationException()
                }

                launch { turret?.trackTarget(limelight, flywheel, drivetrain, snapshot2.goal) }
                launch { flywheel.set(snapshot2.flywheel) }
                launch { feederRoller.set(feederRoller.feedSpeed) }
                launch { shooterHood?.set(snapshot2.hood) }

                repeat(carousel.state.size) {
                    delayUntil { feederSpeed in feederRoller.feedSpeed `±` feederRoller.tolerance }
                    delayUntil { flywheelSpeed in snapshot2.flywheel `±` flywheel.tolerance }
                    fire()
                }
            }

            log(Debug) { "Finished shooting (or timed out), finishing normally" }
            finish()
        }
    }
}