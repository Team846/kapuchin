package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.subsystems.carousel.*
import com.lynbrookrobotics.kapuchin.subsystems.carousel.CarouselMagazineState.Companion.collectSlot
import com.lynbrookrobotics.kapuchin.subsystems.shooter.turret.*
import info.kunalsheth.units.generated.*

suspend fun CarouselComponent.set(target: Angle, tolerance: Angle = 2.Degree) = startRoutine("Set") {
    val current by hardware.position.readOnTick.withoutStamps

    controller {
        PositionOutput(hardware.escConfig, positionGains, hardware.conversions.encoder.native(target))
                .takeUnless { target - current in `±`(tolerance) }
    }
}

//suspend fun FeederRollerComponent.set(target: DutyCycle) = startRoutine("Set") {
//    controller { PercentOutput(hardware.escConfig, target) }
//}