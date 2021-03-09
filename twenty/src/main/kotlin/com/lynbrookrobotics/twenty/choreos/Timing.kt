package com.lynbrookrobotics.twenty.choreos

import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.twenty.Subsystems
import com.lynbrookrobotics.twenty.subsystems.carousel.CarouselComponent
import com.lynbrookrobotics.twenty.subsystems.shooter.flywheel.FlywheelComponent
import info.kunalsheth.units.generated.*

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
) {
    if (flywheel == null || feederRoller == null) {
        log(Error) { "Need flywheel and feeder to run." }
    } else startChoreo("Delay until feeder and flywheel") {

        val flywheelSpeed by flywheel.hardware.speed.readEagerly().withoutStamps
        val feederSpeed by feederRoller.hardware.speed.readEagerly().withoutStamps

        choreography {
            log(Debug) { "Waiting for feeder roller to get up to speed" }
            delayUntil {
                feederSpeed in feederRoller.feedSpeed `±` feederRoller.tolerance
            }
            log(Debug) { "${feederSpeed.Rpm} | ${feederRoller.feedSpeed.Rpm}" }
            log(Debug) { "Feeder roller set" }

            log(Debug) { "Waiting for flywheel to get up to speed" }
            delayUntil {
                flywheelSpeed in flywheelTarget `±` flywheel.tolerance
            }
            log(Debug) { "${flywheelSpeed.Rpm} | ${flywheelTarget.Rpm}" }
            log(Debug) { "Flywheel set" }
        }
    }
}