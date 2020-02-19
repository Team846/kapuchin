package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.subsystems.carousel.*
import info.kunalsheth.units.generated.*

suspend fun CarouselComponent.set(targetPosition: Angle, tolerance: Angle = 5.Degree) = startRoutine("Set") {
    val current by hardware.position.readOnTick.withoutStamps

    controller {
        PositionOutput(
                hardware.escConfig, positionGains, hardware.conversions.encoder.native(targetPosition)
        ).takeUnless { targetPosition - current in `±`(tolerance) }
    }
}