package com.lynbrookrobotics.twenty.routines

import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.twenty.subsystems.carousel.CarouselComponent
import info.kunalsheth.units.generated.*

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
