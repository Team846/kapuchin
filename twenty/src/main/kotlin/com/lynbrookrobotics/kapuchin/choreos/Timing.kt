package com.lynbrookrobotics.kapuchin.choreos

import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.subsystems.carousel.*
import com.lynbrookrobotics.kapuchin.subsystems.shooter.flywheel.*
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

    var lastPercentSpeed = 0.Percent
    var lastAcceleration = 0.Rpm / Second
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

            accelerating &&
                    lastAcceleration < hardware.conversions.ballDecelerationThreshold &&
                    lastPercentSpeed < hardware.conversions.ballPercentDropThreshold
        }
    }
}