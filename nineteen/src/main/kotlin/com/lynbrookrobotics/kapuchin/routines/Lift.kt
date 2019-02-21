package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.subsystems.lift.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

suspend fun LiftComponent.set(target: Length, tolerance: Length = 2.Inch) = startRoutine("Set") {

    val current by hardware.position.readOnTick.withoutStamps

    controller {
        with(hardware.conversions.native) {
            PositionOutput(
                    OffloadedPidGains(
                            kP = native(kP),
                            kI = 0.0,
                            kD = native(kD)
                    ), native(target)
            ).takeIf {
                current in target `±` tolerance
            }
        }
    }
}