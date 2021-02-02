package com.lynbrookrobotics.nineteen.routines

import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.OffloadedEscSafeties.Companion.NoSafeties
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.nineteen.subsystems.driver.OperatorHardware
import com.lynbrookrobotics.nineteen.subsystems.lift.LiftComponent
import info.kunalsheth.units.generated.*

suspend fun LiftComponent.set(target: Length, tolerance: Length = 1.Inch) = startRoutine("Set") {

    val current by hardware.position.readOnTick.withoutStamps
    val startTime = currentTime

    controller {
        if (currentTime - startTime > 5.Second) {
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

    controller {
        PercentOutput(hardware.escConfig, liftPrecision)
    }
}