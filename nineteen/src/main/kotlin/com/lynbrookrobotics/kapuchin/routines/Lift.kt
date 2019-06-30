package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.OffloadedEscSafeties.Companion.NoSafeties
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.subsystems.control.*
import com.lynbrookrobotics.kapuchin.subsystems.lift.*
import com.lynbrookrobotics.kapuchin.timing.*
import info.kunalsheth.units.generated.*

fun Lift.set(target: Length, tolerance: Length = 1.Inch) = newRoutine("Set") {

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

fun Lift.set(dutyCycle: DutyCycle) = newRoutine("Set") {
    controller {
        PercentOutput(hardware.escConfig, dutyCycle, NoSafeties)
    }
}

fun Lift.manualOverride(operator: Operator) = newRoutine("Manual override") {

    val liftPrecision by operator.liftPrecision.readEagerly.withoutStamps

    controller {
        PercentOutput(hardware.escConfig, liftPrecision)
    }
}