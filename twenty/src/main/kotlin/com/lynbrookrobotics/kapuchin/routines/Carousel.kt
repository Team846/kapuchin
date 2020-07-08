package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.subsystems.carousel.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlinx.coroutines.launch

suspend fun CarouselComponent.rezero() = startRoutine("Re-Zero") {
    hardware.isZeroed = false
    controller {
        PercentOutput(hardware.escConfig, zeroSpeed).takeUnless { hardware.isZeroed }
    }
}

suspend fun CarouselComponent.set(targetPosition: Angle, tolerance: Angle = 2.5.Degree) = startRoutine("Set") {
    val current by hardware.position.readOnTick.withoutStamps

    controller {
        PositionOutput(
                hardware.escConfig, positionGains, hardware.conversions.encoder.native(targetPosition)
        ).takeUnless { targetPosition - current in `±`(tolerance) }
    }
}

suspend fun CarouselComponent.set(target: DutyCycle) = startRoutine("Set") {
    controller { PercentOutput(hardware.escConfig, target) }
}

suspend fun CarouselComponent.whereAreMyBalls() = startChoreo("Re-Index") {
    val carouselAngle by hardware.position.readEagerly().withoutStamps
    val color by hardware.color.readEagerly().withoutStamps
    val proximity by hardware.proximity.readEagerly().withoutStamps

    choreography {
        rezero()
        val start = carouselAngle.roundToInt(CarouselSlot)
        for (i in 0 until state.size) {
            set(start + i.CarouselSlot - collectSlot)
            val j = launch { set(start + i.CarouselSlot - collectSlot, 0.Degree) }
            delay(0.1.Second)
            state.set(
                    carouselAngle + collectSlot,
                    hardware.conversions.detectingBall(proximity, color)
            )
            j.cancel()
        }
    }
}