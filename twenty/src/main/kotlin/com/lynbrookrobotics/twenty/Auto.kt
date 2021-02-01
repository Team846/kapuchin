package com.lynbrookrobotics.twenty

import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.control.math.drivetrain.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.twenty.choreos.*
import com.lynbrookrobotics.twenty.routines.*
import com.lynbrookrobotics.twenty.subsystems.*
import com.lynbrookrobotics.twenty.subsystems.carousel.*
import com.lynbrookrobotics.twenty.subsystems.shooter.*
import com.lynbrookrobotics.twenty.subsystems.shooter.FlashlightState.*
import com.lynbrookrobotics.twenty.subsystems.shooter.ShooterHoodState.*
import info.kunalsheth.units.generated.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import java.io.File

typealias Auto = Subsystems.() -> suspend CoroutineScope.() -> Unit

val `verify odometry`: Auto = {
    choreography {
        val traj = pathToTrajectory(
            interpolatePath(
                listOf(
                    Waypoint(0.Foot, 0.Foot),
                    Waypoint(0.Foot, 4.Foot),
                    Waypoint(4.Foot, 4.Foot),
                    Waypoint(4.Foot, 8.Foot)
                ), 6.Inch
            ),
            4.FootPerSecond, drivetrain.maxOmega / 3, 4.FootPerSecondSquared
        )

        drivetrain.followTrajectory(traj, 12.Inch, 2.Inch, reverse = false)
        freeze()
    }
}

val `wall`: Auto = {
    choreography {
        drivetrain.followTrajectory(fastAsFuckLine(4.Foot), 15.Inch, 2.Inch, reverse = false)
    }
}

val `shoot wall`: Auto = {
    choreography {
        withTimeout(12.Second) { autoLimelightAimAndFire() }
        drivetrain.followTrajectory(fastAsFuckLine(4.Foot), 15.Inch, 2.Inch, reverse = false)
    }
}

val `I1 shoot C1 I2 shoot`: Auto = {
    choreography {
        val trajI1C1 = fastAsFuck("I1C1", 40.Percent) ?: fastAsFuckLine(16.Foot, 40.Percent).also {
            log(Error) { "Path I1C1 doesn't exist, fallbacking to 16 feet line." }
        }

        val trajC1I2 = fastAsFuck("C1I2") ?: fastAsFuckLine(16.Foot).also {
            log(Error) { "Path C1I2 doesn't exist, fallbacking to 16 foot line to I1" }
        }

        withTimeout(9.Second) { autoLimelightAimAndFire() }

        val turretJob = launch { turret?.rezero(electrical) }
        val finishedReindex = withTimeout(4.Second) { carousel.whereAreMyBalls() } != null

        val collectJob = launch { if (finishedReindex) eat() }
        drivetrain.followTrajectory(trajI1C1, 12.Inch, 2.Inch, reverse = true)

        collectJob.cancel()

        drivetrain.followTrajectory(trajC1I2, 14.Inch, 2.Inch, reverse = false)
        autoLimelightAimAndFire()

        turretJob.join()
    }
}

val `I2 shoot C1 I2 shoot`: Auto = {
    choreography {
        val trajI2C1 = fastAsFuck("I2C1", 40.Percent)
        if (trajI2C1 == null) {
            log(Error) { "Path I2C1 doesn't exist, running shoot wall" }
            `shoot wall`()
        } else {
            val trajC1I2 = fastAsFuck("C1I2") ?: fastAsFuckLine(16.Foot).also {
                log(Error) { "Path C1I2 doesn't exist, fallbacking to 16 foot line to I1" }
            }

            withTimeout(9.Second) { autoLimelightAimAndFire() }

            val turretJob = launch { turret?.rezero(electrical) }
            val finishedReindex = withTimeout(4.Second) { carousel.whereAreMyBalls() } != null

            val collectJob = launch { if (finishedReindex) eat() }
            drivetrain.followTrajectory(trajI2C1, 12.Inch, 2.Inch, reverse = true)

            collectJob.cancel()

            drivetrain.followTrajectory(trajC1I2, 14.Inch, 2.Inch, reverse = false)
            autoLimelightAimAndFire()

            turretJob.join()
        }
    }
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

private fun Subsystems.fastAsFuckLine(dist: Length, speedFactor: DutyCycle = 100.Percent): Trajectory =
    pathToTrajectory(
        nSect(Waypoint(0.Foot, 0.Foot), Waypoint(0.Foot, dist), 3.Inch),
        drivetrain.maxSpeed * speedFactor,
        drivetrain.percentMaxOmega * drivetrain.maxOmega * speedFactor,
        drivetrain.maxAcceleration
    )

private fun Subsystems.fastAsFuck(name: String, speedFactor: Dimensionless = 100.Percent): Trajectory? =
    loadPath(name)?.let {
        pathToTrajectory(
            it,
            drivetrain.maxSpeed * speedFactor,
            drivetrain.percentMaxOmega * drivetrain.maxOmega * speedFactor,
            drivetrain.maxAcceleration
        )
    }

private suspend fun Subsystems.autoFallbackAimAndFire() {
    val flywheelTarget = 7500.Rpm
    log(Error) { "Limelight wasn't working, falling back to firing at ${flywheelTarget.Rpm}" }

    if (flywheel == null || feederRoller == null) {
        log(Error) { "Need flywheel and feeder to run auto." }
        freeze()
    } else startChoreo("Auto fallback aim and fire") {

        val flywheelSpeed by flywheel.hardware.speed.readEagerly().withoutStamps
        val feederSpeed by feederRoller.hardware.speed.readEagerly().withoutStamps
        val carouselAngle by carousel.hardware.position.readEagerly().withoutStamps

        choreography {
            val carouselRezeroJob = launch { carousel.rezero() }

            scope.launch { withTimeout(7.Second) { flashlight?.set(On) } }

            launch { flywheel.set(flywheelTarget) }
            launch { feederRoller.set(feederRoller.feedSpeed) }
            launch { shooterHood?.set(Up) }

            withTimeout(1.Second) {
                carouselRezeroJob.join()
            }

            repeat(carousel.state.size) { i ->
                val slot = i.CarouselSlot

                log(Debug) { "Waiting for feeder roller to get up to speed" }
                withTimeout(1.5.Second) {
                    delayUntil { feederSpeed in feederRoller.feedSpeed `±` feederRoller.tolerance }
                } ?: log(Error) {
                    "Feeder roller never got up to speed (target = ${
                    feederRoller.feedSpeed.Rpm withDecimals 0
                    } RPM, current = ${
                    feederSpeed.Rpm withDecimals 0
                    })"
                }

                log(Debug) { "Waiting for flywheel to get up to speed" }
                withTimeout(1.5.Second) {
                    delayUntil { flywheelSpeed in flywheelTarget `±` flywheel.tolerance }
                } ?: log(Error) {
                    "Flywheel never got up to speed (target = ${
                    flywheelTarget.Rpm withDecimals 0
                    } RPM, current = ${
                    flywheelSpeed.Rpm withDecimals 0
                    })"
                }

                launch { carousel.set(slot - carousel.shootSlot, 0.CarouselSlot) }

                log(Debug) { "Waiting for ball to launch." }
                withTimeout(1.Second) {
                    flywheel.delayUntilBall()
                } ?: log(Error) { "Did not detect ball launch. Assuming slot was actually empty." }
                carousel.state.set(carouselAngle + carousel.shootSlot, false)
            }

            coroutineContext[Job]!!.cancelChildren()
        }
    }
}

private suspend fun Subsystems.autoLimelightAimAndFire() {
    if (flywheel == null || feederRoller == null || turret == null) {
        log(Error) { "Need flywheel and feeder to run auto." }
        freeze()
    } else startChoreo("Auto limelight aim and fire") {

        val reading by limelight.hardware.readings.readEagerly().withoutStamps
        val pipeline by limelight.hardware.pipeline.readEagerly().withoutStamps
        val robotPosition by drivetrain.hardware.position.readEagerly().withoutStamps
        val turretPosition by turret.hardware.position.readEagerly().withoutStamps
        val flywheelSpeed by flywheel.hardware.speed.readEagerly().withoutStamps
        val feederSpeed by feederRoller.hardware.speed.readEagerly().withoutStamps
        val carouselAngle by carousel.hardware.position.readEagerly().withoutStamps

        choreography {
            launch { drivetrain.set(0.Percent) }
            val carouselRezeroJob = launch { carousel.rezero() }

            scope.launch { withTimeout(7.Second) { flashlight?.set(On) } }

            val pipeline1 = pipeline
            if (pipeline1 == null) {
                log(Error) { "Limelight pipeline1 == $pipeline1" }
                coroutineContext[Job]!!.cancelChildren()
                autoFallbackAimAndFire()
                return@choreography
            }
            launch { limelight.set(pipeline1) }

            val reading1 = reading
            if (reading1 == null) {
                log(Error) { "Limelight reading1 == $reading1" }
                coroutineContext[Job]!!.cancelChildren()
                autoFallbackAimAndFire()
                return@choreography
            }

            val snapshot1 = bestShot(limelight.hardware.conversions.goalPositions(reading1, robotPosition.bearing))
            if (snapshot1 == null) {
                log(Warning) { "Couldn't find snapshot1 or no shots possible" }
                coroutineContext[Job]!!.cancelChildren()
                autoFallbackAimAndFire()
                return@choreography
            }

            launch {
                turret.fieldOrientedPosition(
                    drivetrain,
                    turretPosition + reading1.tx + limelight.hardware.conversions.mountingBearing
                )
            }
            launch { flywheel.set(snapshot1.flywheel) }
            launch { feederRoller.set(feederRoller.feedSpeed) }
            launch { shooterHood?.set(snapshot1.hood) }

            withTimeout(1.Second) {
                carouselRezeroJob.join()
            }

            repeat(carousel.state.size) { i ->
                val slot = i.CarouselSlot

                log(Debug) { "Waiting for feeder roller to get up to speed" }
                withTimeout(1.5.Second) {
                    delayUntil { feederSpeed in feederRoller.feedSpeed `±` feederRoller.tolerance }
                } ?: log(Error) {
                    "Feeder roller never got up to speed (target = ${
                    feederRoller.feedSpeed.Rpm withDecimals 0
                    } RPM, current = ${
                    feederSpeed.Rpm withDecimals 0
                    })"
                }

                log(Debug) { "Waiting for flywheel to get up to speed" }
                withTimeout(1.5.Second) {
                    delayUntil { flywheelSpeed in snapshot1.flywheel `±` flywheel.tolerance }
                } ?: log(Error) {
                    "Flywheel never got up to speed (target = ${
                    snapshot1.flywheel.Rpm withDecimals 0
                    } RPM, current = ${
                    flywheelSpeed.Rpm withDecimals 0
                    })"
                }

                launch { carousel.set(slot - carousel.shootSlot, 0.CarouselSlot) }

                log(Debug) { "Waiting for ball to launch." }
                withTimeout(1.Second) {
                    flywheel.delayUntilBall()
                } ?: log(Error) { "Did not detect ball launch. Assuming slot was actually empty." }
                carousel.state.set(carouselAngle + carousel.shootSlot, false)
            }

            coroutineContext[Job]!!.cancelChildren()
        }
    }
}