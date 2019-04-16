package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.OffloadedEscSafeties.Companion.NoSafeties
import com.lynbrookrobotics.kapuchin.subsystems.driver.*
import com.lynbrookrobotics.kapuchin.subsystems.lift.*
import com.lynbrookrobotics.kapuchin.logging.log
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.timing.currentTime
import info.kunalsheth.units.generated.*

suspend fun LiftComponent.set(target: Length, tolerance: Length = 1.Inch) = startRoutine("Set") {

    val current by hardware.position.readOnTick.withoutStamps
    val startTime = currentTime

    controller {
        if(currentTime - startTime > 2.Second) {
            log(Warning) { "Killing set routine to cool motor" }
            null
        } else PositionOutput(
                hardware.escConfig, positionGains,
                hardware.conversions.native.native(target)
        ).takeUnless {
                    (target - current).abs < tolerance
        }
    }
}

suspend fun LiftComponent.set(dutyCycle: DutyCycle) = startRoutine("Set Openloop") {
    controller {
        PercentOutput(
                hardware.escConfig, dutyCycle, NoSafeties
        )
    }
}

suspend fun LiftComponent.manualOverride(operator: OperatorHardware) = startRoutine("Manual override") {

    val liftPrecision by operator.liftPrecision.readEagerly.withoutStamps
    val position by hardware.position.readEagerly.withoutStamps

    var targetting = position.also {}
    controller {
        if (liftPrecision.isZero) PositionOutput(
                hardware.escConfig, positionGains,
                hardware.conversions.native.native(targetting)
        )
        else {
            targetting = position + 2.Inch * liftPrecision.signum
            PercentOutput(hardware.escConfig, liftPrecision)
        }
    }
}