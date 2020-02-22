package com.lynbrookrobotics.kapuchin.choreos

import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.subsystems.carousel.*
import com.lynbrookrobotics.kapuchin.subsystems.shooter.flywheel.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

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

    choreography {
        delayUntil(poll = 10.milli(Second)) {
            dvdt(speed.x, speed.y) < ballThreshold
        }
    }
}