package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.electrical.*
import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.collector.*
import com.lynbrookrobotics.kapuchin.subsystems.control.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

suspend fun CollectorPivot.set(target: CollectorPivotState) = startRoutine("Set") {
    controller { target }
}

suspend fun CollectorRollers.spin(electrical: Electrical, top: V, bottom: V = top) = startRoutine("Spin") {
    val vBat by electrical.batteryVoltage.readEagerly.withoutStamps

    controller {
        TwoSided(
                voltageToDutyCycle(top, vBat),
                voltageToDutyCycle(bottom, vBat)
        )
    }
}

suspend fun CollectorRollers.set(state: TwoSided<DutyCycle>) = startRoutine("Set") {
    controller { state }
}

suspend fun CollectorSlider.set(target: Length, electrical: Electrical, tolerance: Length = 0.5.Inch) = startRoutine("Set") {

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

suspend fun CollectorSlider.trackLine(lineScanner: LineScanner, electrical: Electrical) = startRoutine("Track line") {

    val target by lineScanner.linePosition.readOnTick.withoutStamps
    val current by hardware.position.readOnTick.withoutStamps
    val vBat by electrical.batteryVoltage.readEagerly.withoutStamps

    controller {
        target?.let { snapshot ->
            val error = snapshot - current
            val voltage = kP * error

            voltageToDutyCycle(
                    voltage cap `±`(operatingVoltage),
                    vBat,
                    false
            )
        } ?: 0.Percent
    }
}

suspend fun CollectorSlider.set(target: DutyCycle) = startRoutine("Manual Override") {
    controller { target }
}

suspend fun CollectorSlider.reZero() = startChoreo("Re-Zero") {
    hardware.isZeroed = false
    choreography {
        while (!hardware.isZeroed) delay(0.2.Second)
    }
}

suspend fun CollectorSlider.manualOverride(operator: Operator) = startRoutine("Manual Override") {
    val sliderPrecision by operator.sliderPrecision.readEagerly().withoutStamps
    controller { sliderPrecision }
}

suspend fun Hook.set(target: HookPosition) = startRoutine("Set") {
    controller { target }
}

suspend fun HookSlider.set(target: HookSliderState) = startRoutine("Set") {
    controller { target }
}