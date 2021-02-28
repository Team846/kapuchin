package com.lynbrookrobotics.twenty.choreos

import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.twenty.Subsystems
import com.lynbrookrobotics.twenty.subsystems.carousel.CarouselComponent
import com.lynbrookrobotics.twenty.subsystems.shooter.flywheel.FlywheelComponent
import info.kunalsheth.units.generated.*
import kotlinx.coroutines.launch

suspend fun CarouselComponent.delayUntilBall() = startChoreo("Delay Until Ball") {
    val color by hardware.color.readEagerly().withoutStamps
    val proximity by hardware.proximity.readEagerly().withoutStamps

    choreography {
        delayUntil {
            hardware.conversions.detectingBall(proximity, color)
        }
    }
}

suspend fun FlywheelComponent.delayUntilBall() = startChoreo("Delay Until Ball") {
    val speed by hardware.speed.readEagerly().withStamps
    val dvdt = differentiator(::p, speed.x, speed.y)

    val initialSpeed = speed.y

    var lastAcceleration = 0.Rpm / Second
    var lastPercentSpeed = 0.Percent

    choreography {
        delayUntil(clock) {
            val acceleration = dvdt(speed.x, speed.y)
            val percentSpeed = (speed.y - initialSpeed) / initialSpeed

            val accelerating = percentSpeed > lastPercentSpeed
            if (acceleration > lastAcceleration) log(Debug) {
                "Peak deceleration: ${lastAcceleration.RpmPerSecond withDecimals 2} RPM/sec"
            }

            if (percentSpeed > lastPercentSpeed) log(Debug) {
                "Peak percent drop: ${lastPercentSpeed.Percent withDecimals 1}%"
            }

            (accelerating &&
                    lastAcceleration < hardware.conversions.ballDecelerationThreshold &&
                    lastPercentSpeed < hardware.conversions.ballPercentDropThreshold)

                .also {
                    lastAcceleration = acceleration
                    lastPercentSpeed = percentSpeed
                }
        }
    }
}

suspend fun Subsystems.delayUntilFeederAndFlywheel(
    flywheelTarget: AngularVelocity,
): Boolean {

    var feederSet = false
    var flywheelSet = false

    if (flywheel == null || feederRoller == null) {
        log(Error) { "Need flywheel and feeder to run." }
    } else startChoreo("Delay until feeder and flywheel") {

        val flywheelSpeed by flywheel.hardware.speed.readEagerly().withoutStamps
        val feederSpeed by feederRoller.hardware.speed.readEagerly().withoutStamps

        choreography {
            val feederJob = launch {
                log(Debug) { "Waiting for feeder roller to get up to speed" }
                delayUntil {
                    feederSpeed in feederRoller.feedSpeed `±` feederRoller.tolerance
                }
                log(Debug) { "Feeder roller set" }
                feederSet = true
            }

            val flywheelJob = launch {
                log(Debug) { "Waiting for flywheel to get up to speed" }
                delayUntil {
                    flywheelSpeed in flywheelTarget `±` flywheel.tolerance
                }
                log(Debug) { "Flywheel set" }
                flywheelSet = true
            }

            withTimeout(3.Second) {
                feederJob.join()
                flywheelJob.join()
            }

            if (!feederSet) {
                log(Error) {
                    "Feeder roller never got up to speed (target = ${
                        feederRoller.feedSpeed.Rpm withDecimals 0
                    } RPM, current = ${
                        feederSpeed.Rpm withDecimals 0
                    })"
                }
            }

            if (!flywheelSet) {
                log(Error) {
                    "Flywheel never got up to speed (target = ${
                        flywheelTarget.Rpm withDecimals 0
                    } RPM, current = ${
                        flywheelSpeed.Rpm withDecimals 0
                    })"
                }
            }
        }
    }

    return feederSet && flywheelSet
}