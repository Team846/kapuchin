package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.subsystems.carousel.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

suspend fun CarouselComponent.set(targetPosition: Angle, tolerance: Angle = 5.Degree) = startRoutine("Set") {
    val current by hardware.position.readOnTick.withoutStamps

    controller {
        PositionOutput(
                hardware.escConfig, positionGains, hardware.conversions.encoder.native(targetPosition)
        ).takeUnless { targetPosition - current in `±`(tolerance) }
    }
}

suspend fun CarouselComponent.whereAreMyBalls() = startChoreo("Carousel Reindex") {
    val carouselAngle by hardware.position.readEagerly().withoutStamps
    val color by hardware.color.readEagerly().withoutStamps
    val proximity by hardware.proximity.readEagerly().withoutStamps

    choreography {
        val start = carouselAngle.roundToInt(CarouselSlot)
        for (i in 0 until state.size) {
            set(start + i.CarouselSlot - colorSensor)
            state.set(
                    carouselAngle + colorSensor,
                    hardware.conversions.detectingBall(proximity, color)
            )
        }
    }
}