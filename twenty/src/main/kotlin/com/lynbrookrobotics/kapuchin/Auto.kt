package com.lynbrookrobotics.kapuchin

import com.lynbrookrobotics.kapuchin.choreos.*
import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.control.math.drivetrain.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.subsystems.carousel.*
import com.lynbrookrobotics.kapuchin.subsystems.shooter.*
import info.kunalsheth.units.generated.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File

typealias Auto = Subsystems.() -> suspend CoroutineScope.() -> Unit

val `verify odometry`: Auto = {
    choreography {
        val traj = pathToTrajectory(
                interpolatePath(listOf(
                        Waypoint(0.Foot, 0.Foot),
                        Waypoint(0.Foot, 4.Foot),
                        Waypoint(4.Foot, 4.Foot),
                        Waypoint(4.Foot, 8.Foot)
                ), 6.Inch),
                4.FootPerSecond, drivetrain.maxOmega / 3, 4.FootPerSecondSquared
        )

        genericAuto(traj, reverse = false, collect = false, rezero = true, shootTimeout = 10.Second)
    }
}

val `shoot wall`: Auto = {
    choreography {
        genericAuto(
                fastAsFuckLine(4.Foot),
                reverse = false, collect = false, rezero = true, shootTimeout = 10.Second
        )
    }
}


val `I1 shoot C1`: Auto = {
    choreography {
        val trajI1C1 = fastAsFuck("I1C1", 40.Percent) ?: fastAsFuckLine(16.Foot, 40.Percent).also {
            log(Error) { "Path I1C1 doesn't exist, fallbacking to 16 feet" }
        }
        genericAuto(trajI1C1, reverse = true, collect = true, rezero = true, shootTimeout = 10.Second)
    }
}

val `I2 shoot C1`: Auto = {
    choreography {
        val trajI2C1 = fastAsFuck("I2C1", 40.Percent)
        if (trajI2C1 == null) {
            log(Error) { "Path I2C1 doesn't exist, running shoot wall" }
            `shoot wall`()
        } else genericAuto(trajI2C1, reverse = true, collect = true, rezero = true, shootTimeout = 10.Second)
    }
}

//val `I3 shoot C5`: Auto = {
//    choreography {
//        val trajI3C5 = fastAsFuck("I3C5", 40.Percent)
//        if (trajI3C5 == null) {
//            log(Error) { "Path I3C5 doesn't exist, running shoot wall" }
//            `shoot wall`()
//        } else genericAuto(trajI3C5, reverse = false, collect = true, rezero = true, shootTimeout = 10.Second)
//    }
//}

val `I1 shoot C1 I2 shoot`: Auto = {
    choreography {
        val trajC1I2 = fastAsFuck("C1I2") ?: fastAsFuckLine(8.Foot).also {
            log(Error) { "Path C1I2 doesn't exist, fallbacking to 8 foot line" }
        }

        `I1 shoot C1`()
        drivetrain.followTrajectory(trajC1I2, 15.Inch, 5.Inch, reverse = false)
        genericAuto(null, reverse = false, collect = false, rezero = false, shootTimeout = 15.Second)
    }
}

val `I2 shoot C1 I2 shoot`: Auto = {
    choreography {
        val trajC1I2 = fastAsFuck("C1I2") ?: fastAsFuckLine(8.Foot).also {
            log(Error) { "Path C1I2 doesn't exist, fallbacking to 8 foot line" }
        }

        `I2 shoot C1`()
        drivetrain.followTrajectory(trajC1I2, 15.Inch, 5.Inch, reverse = false)
        genericAuto(null, reverse = false, collect = false, rezero = false, shootTimeout = 15.Second)
    }
}

//val `I3 shoot C5 I3 shoot`: Auto = {
//    choreography {
//        val pathC5I3 = fastAsFuck("C5I3")
//        if (pathC5I3 == null) log(Error) { "C5I3 path doesn't exist!!" }
//        `I3 shoot C5`()
//        pathC5I3?.let { drivetrain.followTrajectory(it, 15.Inch, 5.Inch, reverse = false) }
//        genericAuto(null, reverse = false, collect = false, rezero = false, shootTimeout = 15.Second)
//    }
//}

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

/**
 * Trajectory of a line at max limits.
 */
private fun Subsystems.fastAsFuckLine(dist: Length, speedFactor: DutyCycle = 100.Percent): Trajectory =
        pathToTrajectory(
                nSect(Waypoint(0.Foot, 0.Foot), Waypoint(0.Foot, dist), 3.Inch),
                drivetrain.maxSpeed * speedFactor, drivetrain.percentMaxOmega * drivetrain.maxOmega * speedFactor, drivetrain.maxAcceleration
        )

/**
 * Trajectory of the named path at max limits, null if path TSV doesn't exist.
 */
private fun Subsystems.fastAsFuck(name: String, speedFactor: DutyCycle = 100.Percent): Trajectory? = loadPath(name)?.let {
    pathToTrajectory(it, drivetrain.maxSpeed * speedFactor, drivetrain.percentMaxOmega * drivetrain.maxOmega * speedFactor, drivetrain.maxAcceleration)
}

/**
 * A generic auto that shoots, drives, and collects.
 *
 * @param endingTrajectory the trajectory to follow AFTER shooting balls.
 * @param reverse whether or not to follow trajectory in reverse (reverse = intake on front).
 * @param collect whether or not to collect balls while following the trajectory.
 * @param rezero whether or not to rezero the turret and reindex the carousel while following the trajectory.
 * @param shootTimeout timeout on shooting before rezero-ing and following trajectory.
 */
private suspend fun Subsystems.genericAuto(
        endingTrajectory: Trajectory?,
        reverse: Boolean,
        collect: Boolean,
        rezero: Boolean,
        shootTimeout: Time
) {
    if (flywheel == null || feederRoller == null) {
        log(Error) { "Need flywheel and feeder to run auto." }
        freeze()
    } else startChoreo("Generic Auto") {

        val reading by limelight.hardware.readings.readEagerly().withoutStamps
        val robotPosition by drivetrain.hardware.position.readEagerly().withoutStamps
        val flywheelSpeed by flywheel.hardware.speed.readEagerly().withoutStamps
        val feederSpeed by feederRoller.hardware.speed.readEagerly().withoutStamps
        val carouselAngle by carousel.hardware.position.readEagerly().withoutStamps

        val jobs = mutableListOf<Job>()

        choreography {
            carousel.rezero()

            withTimeout(shootTimeout) {
                val reading1 = reading
                if (reading1?.pipeline == null) {
                    log(Error) { "Limelight pipeline is null. Finishing early." }
                    return@withTimeout
                }

                jobs += launch { limelight.set(reading1.pipeline) }

                val snapshot1 = bestShot(limelight.hardware.conversions.goalPositions(reading1, robotPosition.bearing))
                if (snapshot1 == null) {
                    log(Error) { "Couldn't find snapshot1 or no shots possible. Finishing early." }
                    return@withTimeout
                }

                withTimeout(2.Second) { turret?.trackTarget(limelight, flywheel, drivetrain, snapshot1.goal, 1.Degree) }
                jobs += launch { turret?.trackTarget(limelight, flywheel, drivetrain, snapshot1.goal) }
                withTimeout(1.Second) { limelight.autoZoom() }

                val snapshot2 = reading?.let { bestShot(limelight.hardware.conversions.goalPositions(it, robotPosition.bearing)) }
                if (snapshot2 == null) {
                    log(Error) { "Couldn't find snapshot2 or no shots possible. Finishing early." }
                    return@withTimeout
                }

                jobs += launch { turret?.trackTarget(limelight, flywheel, drivetrain, snapshot2.goal) }
                jobs += launch { flywheel.set(snapshot2.flywheel) }
                jobs += launch { feederRoller.set(feederRoller.feedSpeed) }
                jobs += launch { shooterHood?.set(snapshot2.hood) }

                repeat(carousel.state.size) {
                    val slot = carousel.state.closestEmpty(carouselAngle + carousel.shootSlot)
                    if (slot == null) {
                        log(Error) { "Cannot find an empty slot before reindex." }
                        return@withTimeout
                    }

                    delayUntil { feederSpeed in feederRoller.feedSpeed `±` feederRoller.tolerance }
                    delayUntil { flywheelSpeed in snapshot2.flywheel `±` flywheel.tolerance }

                    val fireJob = launch { carousel.set(slot - carousel.shootSlot, 0.CarouselSlot) }

                    log(Debug) { "Firing slot #$it, waiting for ball to launch." }
                    withTimeout(1.Second) {
                        flywheel.delayUntilBall()
                    } ?: log(Debug) { "Did not detect ball launch. Going to next slot." }

                    fireJob.cancel()
                }

                log(Debug) { "Finished shooting loop." }
            }

            log(Debug) { "Running end of generic auto." }

            val carouselJob = launch { if (rezero) carousel.whereAreMyBalls() }.also { jobs += it }
            jobs += launch { if (rezero) turret?.rezero(electrical) }

            if (collect && rezero) {
                log(Debug) { "Waiting for carousel to rezero before collection" }
                carouselJob.join()
            }

            val collectJob = launch { if (collect) eat() }
            endingTrajectory?.let { drivetrain.followTrajectory(it, 12.Inch, 2.Inch, reverse) }

            collectJob.cancel()
            carouselJob.join()

            jobs.forEach { it.cancel() }
        }
    }
}