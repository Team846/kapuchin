package com.lynbrookrobotics.twenty.choreos.auto

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.twenty.Subsystems
import com.lynbrookrobotics.twenty.choreos.*
import com.lynbrookrobotics.twenty.routines.*
import com.lynbrookrobotics.twenty.subsystems.carousel.CarouselSlot
import com.lynbrookrobotics.twenty.subsystems.shooter.ShooterHoodState.Up
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlinx.coroutines.launch
import kotlin.system.measureTimeMillis

suspend fun Subsystems.timePath() = startChoreo("Time Path") {
    choreography {
        with(drivetrain) {
            loadRobotPath(autoPath)?.let { path ->
                val time = measureTimeMillis {
                    followTrajectory(
                        fastAsFuckPath(path),
                        maxExtrapolate = maxExtrapolate,
                        safetyTolerance = 3.Foot,
                        reverse = false,
                    )
                }.milli(Second)
                log(Debug) { "Path finished: ${time.Second}s" }
            } ?: log(Error) { "Couldn't find path $name" }
        }
    }
}

suspend fun Subsystems.timeTrajectory() = startChoreo("Time Trajectory") {
    choreography {
        with(drivetrain) {
            loadRobotTrajectory(autoTrajectory)?.let { traj ->
                val time = measureTimeMillis {
                    followTrajectory(
                        traj.map { it.copy(x = it.x / speedFactor) },
                        maxExtrapolate = maxExtrapolate,
                        safetyTolerance = 3.Foot,
                        reverse = false,
                    )
                }.milli(Second)
                log(Debug) { "Trajectory finished: ${time.Second}s" }
            } ?: log(Error) { "Couldn't find trajectory $name" }
        }
    }
}

suspend fun Subsystems.judgedAuto() {
    val flywheelTarget = 6000.Rpm

    if (flywheel == null || feederRoller == null) {
        log(Error) { "Need flywheel and feeder to run auto." }
        freeze()
    } else startChoreo("Judged Auto") {

        val flywheelSpeed by flywheel.hardware.speed.readEagerly().withoutStamps
        val feederSpeed by feederRoller.hardware.speed.readEagerly().withoutStamps
        val carouselAngle by carousel.hardware.position.readEagerly().withoutStamps

        choreography {
            launch { turret?.fieldOrientedPosition(drivetrain, UomVector(-26.Foot, 0.Foot)) }
            carousel.rezero()
            carousel.whereAreMyBalls()


            val intakeJob = launch { intakeBalls() }
            val path = loadRobotPath("judged_arc1")
            if (path == null) {
                log(Error) { "Unable to find arc1 path" }

//                coroutineContext.job.cancelChildren()
//                return@choreography
            }
            path?.let {
                drivetrain.followTrajectory(
                    fastAsFuckPath(it, speedFactor = 40.Percent),
                    maxExtrapolate = drivetrain.maxExtrapolate,
                    reverse = true
                )
            }

            intakeJob.cancel()

            launch { visionAim() }
            launch { feederRoller.set(0.Rpm) }

            val fullSlot = carousel.state.closestFull(carouselAngle + carousel.shootSlot)
            if (fullSlot != null) {
                val target = fullSlot - carousel.shootSlot
                if (target > carouselAngle) carousel.set(target - 0.5.CarouselSlot)
                if (target < carouselAngle) carousel.set(target + 0.5.CarouselSlot)
            }

            launch { flywheel.set(flywheelTarget) }
            launch { feederRoller.set(feederRoller.feedSpeed) }

            fun feederCheck() = feederSpeed in feederRoller.feedSpeed `±` feederRoller.tolerance
            fun flywheelCheck() = flywheelSpeed in flywheelTarget `±` flywheel.tolerance

            log(Debug) { "Waiting for feeder roller to get up to speed" }
            withTimeout(5.Second) {
                delayUntil(predicate = ::feederCheck)
            } ?: log(Error) {
                "Feeder roller never got up to speed (target = ${
                    feederRoller.feedSpeed.Rpm withDecimals 0
                } RPM, current = ${
                    feederSpeed.Rpm withDecimals 0
                })"
            }

            log(Debug) { "Waiting for flywheel to get up to speed" }
            withTimeout(5.Second) {
                delayUntil(predicate = ::flywheelCheck)
            } ?: log(Error) {
                "Flywheel never got up to speed (target = ${
                    flywheelTarget.Rpm withDecimals 0
                } RPM, current = ${
                    flywheelSpeed.Rpm withDecimals 0
                })"
            }

            println(flywheelSpeed.Rpm)
            log(Debug) { "Feeder roller and flywheel set" }
            launch { shooterHood?.set(Up) }
            launch { carousel.set(carousel.fireAllDutycycle) }
        }
    }
}

suspend fun Subsystems.debug() = startChoreo("Debug Auto") {
    choreography {
        turret?.fieldOrientedPosition(drivetrain, UomVector(0.Foot, 5.Foot))
    }
}