package com.lynbrookrobotics.kapuchin

import com.lynbrookrobotics.kapuchin.choreos.*
import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.control.math.drivetrain.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.subsystems.shooter.*
import info.kunalsheth.units.generated.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import java.io.File

suspend fun Subsystems.`shoot wall`() = genericAuto(fastAsFuckLine(4.Foot), reverse = false, collect = false)

suspend fun Subsystems.`I1 shoot C1`() = genericAuto(fastAsFuck("I1C1"), reverse = true, collect = true)

suspend fun Subsystems.`I2 shoot C1`() = genericAuto(fastAsFuck("I2C1"), reverse = true, collect = true)

suspend fun Subsystems.`I3 shoot C5`() = genericAuto(fastAsFuck("I3C5"), reverse = true, collect = true)

suspend fun Subsystems.`I1 shoot C1 I2 shoot`() {
    val pathC1I2 = fastAsFuck("C1I2")

    `I1 shoot C1`()
    drivetrain.followTrajectory(pathC1I2, 15.Inch, 5.Inch, reverse = false)
    genericAuto(null, reverse = false, collect = false)
}

suspend fun Subsystems.`I3 shoot C5 I3 shoot`() {
    val pathC5I3 = fastAsFuck("C5I3")

    `I3 shoot C5`()
    drivetrain.followTrajectory(pathC5I3, 15.Inch, 5.Inch, reverse = false)
    genericAuto(null, reverse = false, collect = false)
}

private fun loadPath(name: String) = Thread.currentThread()
        .contextClassLoader
        .getResourceAsStream("com/lynbrookrobotics/kapuchin/paths/$name.tsv")!!
        .bufferedReader()
        .lineSequence()
        .drop(1)
        .map { it.split('\t') }
        .map { it.map { tkn -> tkn.trim() } }
        .map { Waypoint(it[0].toDouble().Foot, it[1].toDouble().Foot) }
        .toList()

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

private fun Subsystems.fastAsFuck(name: String): Trajectory =
        pathToTrajectory(
                loadPath(name),
                drivetrain.maxSpeed, drivetrain.percentMaxOmega * drivetrain.maxOmega, drivetrain.maxAcceleration
        )

/**
 * Shoots balls then follows `endingTrajectory`
 */
private suspend fun Subsystems.genericAuto(endingTrajectory: Trajectory?, reverse: Boolean, collect: Boolean) {
    if (flywheel == null || feederRoller == null) {
        log(Error) { "Need flywheel and feeder to run auto" }
        freeze()
    } else startChoreo("Generic Auto") {

        val reading by limelight.hardware.readings.readEagerly().withoutStamps
        val robotPosition by drivetrain.hardware.position.readEagerly().withoutStamps
        val flywheelSpeed by flywheel.hardware.speed.readEagerly().withoutStamps
        val feederSpeed by feederRoller.hardware.speed.readEagerly().withoutStamps

        suspend fun finish() = supervisorScope {
            log(Warning) { "Couldn't find target, rezeroing and moving" }
            launch { carousel.whereAreMyBalls() }
            launch { turret?.rezero(electrical) }

            val collectJob = launch { if (collect) eat() }
            endingTrajectory?.let { drivetrain.followTrajectory(it, 12.Inch, 2.Inch, reverse) }
            collectJob.cancel()
        }

        choreography {
            carousel.rezero()

            val snapshot1 = reading?.let { bestShot(limelight.hardware.conversions.goalPositions(it, robotPosition.bearing)) }
            if (snapshot1 == null) {
                log(Warning) { "Couldn't find snapshot1, finishing early" }
                finish()
                return@choreography
            }

            turret?.trackTarget(limelight, flywheel, drivetrain, snapshot1.goal, 1.Degree)
            launch { turret?.trackTarget(limelight, flywheel, drivetrain, snapshot1.goal) }

            val snapshot2 = reading?.let { bestShot(limelight.hardware.conversions.goalPositions(it, robotPosition.bearing)) }
            if (snapshot2 == null) {
                log(Warning) { "Couldn't find snapshot2, finishing early" }
                finish()
                return@choreography
            }

            val aim = launch { visionAim() }

            repeat(carousel.state.ammo) {
                delayUntil { feederSpeed in feederRoller.feedSpeed `±` feederRoller.tolerance }
                delayUntil { flywheelSpeed in snapshot2.flywheel `±` flywheel.tolerance }
                fire()
            }

            aim.cancel()
            finish()
        }
    }
}