package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.subsystems.driver.*
import com.lynbrookrobotics.kapuchin.subsystems.lift.*
import info.kunalsheth.units.generated.*

suspend fun LiftComponent.set(target: Length, tolerance: Length = 2.Inch) = startRoutine("Set") {

    val current by hardware.position.readOnTick.withoutStamps

    controller {
        com.lynbrookrobotics.kapuchin.hardware.offloaded.PositionOutput(
                hardware.escConfig, positionGains,
                hardware.conversions.native.native(target)
        ).takeUnless {
            (target - current).abs < tolerance
        }
    }
}

suspend fun LiftComponent.manualOverride(operator: OperatorHardware) = startRoutine("Manual override") {

    val liftPrecision by operator.liftPrecision.readEagerly.withoutStamps
    val position by hardware.position.readEagerly.withoutStamps

    var targetting = position.also {}
    controller {
        if (liftPrecision.isZero) com.lynbrookrobotics.kapuchin.hardware.offloaded.PositionOutput(
                hardware.escConfig, positionGains,
                hardware.conversions.native.native(targetting)
        )
        else {
            targetting = position + 2.Inch * liftPrecision.signum
            com.lynbrookrobotics.kapuchin.hardware.offloaded.PercentOutput(hardware.escConfig, liftPrecision)
        }
    }
}