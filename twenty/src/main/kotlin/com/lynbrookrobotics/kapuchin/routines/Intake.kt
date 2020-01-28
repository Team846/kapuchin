package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.electrical.*
import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.collector.*
import com.lynbrookrobotics.kapuchin.subsystems.collector.hookslider.*
import com.lynbrookrobotics.kapuchin.subsystems.collector.pivot.*
import com.lynbrookrobotics.kapuchin.subsystems.collector.slider.*
import com.lynbrookrobotics.kapuchin.subsystems.driver.*
import com.lynbrookrobotics.kapuchin.subsystems.drivetrain.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

suspend fun CollectorPivotComponent.set(target: CollectorPivotState) = startRoutine("Set") {
    controller { target }
}

suspend fun CollectorRollersComponent.spin(electrical: ElectricalSystemHardware, top: V, bottom: V = top) = startRoutine("Spin") {
    val vBat by electrical.batteryVoltage.readEagerly.withoutStamps

    controller {
        TwoSided(
                voltageToDutyCycle(top, vBat),
                voltageToDutyCycle(bottom, vBat)
        )
    }
}

suspend fun CollectorRollersComponent.set(state: TwoSided<DutyCycle>) = startRoutine("Set") {
    controller { state }
}

suspend fun CollectorSliderComponent.set(target: Length, electrical: ElectricalSystemHardware, tolerance: Length = 0.5.Inch) = startRoutine("Set") {

    val current by hardware.position.readOnTick.withoutStamps
    val vBat by electrical.batteryVoltage.readEagerly.withoutStamps

    controller {
        val error = target - current
        val voltage = kP * error

        voltageToDutyCycle(voltage, vBat).takeUnless {
            error.abs < tolerance
        }
    }
}

suspend fun CollectorSliderComponent.set(target: DutyCycle) = startRoutine("Manual Override") {
    controller { target }
}

suspend fun CollectorSliderComponent.reZero() = startChoreo("Re-Zero") {
    hardware.isZeroed = false
    choreography {
        while (!hardware.isZeroed) delay(0.2.Second)
    }
}

suspend fun CollectorSliderComponent.manualOverride(operator: OperatorHardware) = startRoutine("Manual Override") {
    val sliderPrecision by operator.sliderPrecision.readEagerly().withoutStamps
    controller { sliderPrecision }
}

suspend fun HookComponent.set(target: HookPosition) = startRoutine("Set") {
    controller { target }
}

suspend fun HookSliderComponent.set(target: HookSliderState) = startRoutine("Set") {
    controller { target }
}