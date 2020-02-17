package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.subsystems.carousel.*
import info.kunalsheth.units.generated.*

suspend fun CarouselComponent.set(targetPosition: Angle, targetState: CarouselMagazineState, tolerance: Angle = 2.Degree) = startRoutine("Set") {
    val current by hardware.position.readOnTick.withoutStamps

    controller {
        Pair(
                PositionOutput(hardware.escConfig, positionGains, hardware.conversions.encoder.native(targetPosition)),
                targetState
        ).takeUnless { targetPosition - current in `±`(tolerance) }
    }
}