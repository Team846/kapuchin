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
import kotlinx.coroutines.*
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

        autoDrive(traj, rezero = true, reverse = false, collect = false)
    }
}

val `shoot wall`: Auto = {
    choreography {
        withTimeout (10.Second) { autoAimAndFire(useCarouselState = false) }
        autoDrive(fastAsFuckLine(4.Foot), reverse = false, collect = false, rezero = true)
    }
}


val `I1 shoot C1`: Auto = {
    choreography {
        val trajI1C1 = fastAsFuck("I1C1", 40.Percent) ?: fastAsFuckLine(16.Foot, 40.Percent).also {
            log(Error) { "Path I1C1 doesn't exist, fallbacking to 16 feet line." }
        }

        withTimeout(10.Second) { autoAimAndFire(useCarouselState = false) }
        autoDrive(trajI1C1, reverse = true, collect = true, rezero = true)
    }
}

val `I2 shoot C1`: Auto = {
    choreography {
        val trajI2C1 = fastAsFuck("I2C1", 40.Percent)
        if (trajI2C1 == null) {
            log(Error) { "Path I2C1 doesn't exist, running shoot wall" }
            `shoot wall`()
        } else {
            withTimeout(10.Second) { autoAimAndFire(useCarouselState = false) }
            autoDrive(trajI2C1, reverse = true, collect = true, rezero = true)
        }
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
        autoDrive(trajC1I2, reverse = false, collect = false, rezero = false)
        autoAimAndFire(useCarouselState = true)
    }
}

val `I2 shoot C1 I2 shoot`: Auto = {
    choreography {
        val trajC1I2 = fastAsFuck("C1I2") ?: fastAsFuckLine(8.Foot).also {
            log(Error) { "Path C1I2 doesn't exist, fallbacking to 8 foot line" }
        }

        `I2 shoot C1`()
        autoDrive(trajC1I2, reverse = false, collect = false, rezero = false)
        autoAimAndFire(useCarouselState = true)
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

private fun Subsystems.fastAsFuckLine(dist: Length, speedFactor: DutyCycle = 100.Percent): Trajectory =
        pathToTrajectory(
                nSect(Waypoint(0.Foot, 0.Foot), Waypoint(0.Foot, dist), 3.Inch),
                drivetrain.maxSpeed * speedFactor, drivetrain.percentMaxOmega * drivetrain.maxOmega * speedFactor, drivetrain.maxAcceleration
        )

private fun Subsystems.fastAsFuck(name: String, speedFactor: DutyCycle = 100.Percent): Trajectory? = loadPath(name)?.let {
    pathToTrajectory(it, drivetrain.maxSpeed * speedFactor, drivetrain.percentMaxOmega * drivetrain.maxOmega * speedFactor, drivetrain.maxAcceleration)
}

private suspend fun Subsystems.autoAimAndFire(useCarouselState: Boolean) {
    if (flywheel == null || feederRoller == null) {
        log(Error) { "Need flywheel and feeder to run auto." }
        freeze()
    } else startChoreo("Auto aim and fire") {

        val reading by limelight.hardware.readings.readEagerly().withoutStamps
        val robotPosition by drivetrain.hardware.position.readEagerly().withoutStamps
        val flywheelSpeed by flywheel.hardware.speed.readEagerly().withoutStamps
        val feederSpeed by feederRoller.hardware.speed.readEagerly().withoutStamps
        val carouselAngle by carousel.hardware.position.readEagerly().withoutStamps

        choreography {
            carousel.rezero()

            val reading1 = reading
            if (reading1?.pipeline == null) {
                log(Error) { "Limelight pipeline is null!" }

                return@choreography
            }

            launch { limelight.set(reading1.pipeline) }

            val snapshot1 = bestShot(limelight.hardware.conversions.goalPositions(reading1, robotPosition.bearing))
            if (snapshot1 == null) {
                log(Warning) { "Couldn't find snapshot1 or no shots possible." }

                coroutineContext[Job]!!.cancelChildren()
                return@choreography
            }

            withTimeout(2.Second) { turret?.trackTarget(limelight, flywheel, drivetrain, snapshot1.goal, 1.Degree) }
            launch { turret?.trackTarget(limelight, flywheel, drivetrain, snapshot1.goal) }
            withTimeout(1.Second) { limelight.autoZoom() }

            val snapshot2 = reading?.let { bestShot(limelight.hardware.conversions.goalPositions(it, robotPosition.bearing)) }
            if (snapshot2 == null) {
                log(Error) { "Couldn't find snapshot2 or no shots possible." }

                coroutineContext[Job]!!.cancelChildren()
                return@choreography
            }

            launch { turret?.trackTarget(limelight, flywheel, drivetrain, snapshot2.goal) }
            launch { flywheel.set(snapshot2.flywheel) }
            launch { feederRoller.set(feederRoller.feedSpeed) }
            launch { shooterHood?.set(snapshot2.hood) }

            repeat(carousel.state.size) { i ->
                val slot = if (useCarouselState) {
                    val fullSlot = carousel.state.closestFull(carouselAngle + carousel.shootSlot)
                    if (fullSlot == null) {
                        log(Debug) { "No more balls." }
                        return@repeat
                    }
                    fullSlot
                } else i.CarouselSlot

                delayUntil { feederSpeed in feederRoller.feedSpeed `±` feederRoller.tolerance }
                delayUntil { flywheelSpeed in snapshot2.flywheel `±` flywheel.tolerance }

                val fireJob = launch { carousel.set(slot - carousel.shootSlot, 0.CarouselSlot) }

                log(Debug) { "Firing slot #$i, waiting for ball to launch." }
                withTimeout(1.Second) {
                    flywheel.delayUntilBall()
                } ?: log(Debug) { "Did not detect ball launch. Going to next slot." }

                fireJob.cancel()
            }

            coroutineContext[Job]!!.cancelChildren()
        }
    }
}

private suspend fun Subsystems.autoDrive(
        trajectory: Trajectory,
        rezero: Boolean,
        reverse: Boolean,
        collect: Boolean
) = coroutineScope {

    val carouselJob = launch { if (rezero) carousel.whereAreMyBalls() }
    launch { if (rezero) turret?.rezero(electrical) }

    if (collect && rezero) {
        log(Debug) { "Waiting for carousel to rezero before collection" }
        carouselJob.join()
    }

    val collectJob = launch { if (collect) eat() }
    drivetrain.followTrajectory(trajectory, 12.Inch, 2.Inch, reverse)

    collectJob.cancel()
    carouselJob.join()

    coroutineContext[Job]!!.cancelChildren()
}