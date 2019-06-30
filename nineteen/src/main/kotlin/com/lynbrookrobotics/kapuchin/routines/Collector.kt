package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.electrical.*
import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.collector.*
import com.lynbrookrobotics.kapuchin.subsystems.control.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

fun CollectorPivot.set(target: CollectorPivotState) = newRoutine("Set") {
    controller { target }
}

fun CollectorRollers.set(electrical: Electrical, top: V, bottom: V = top) = newRoutine("Set") {
    val vBat by electrical.batteryVoltage.readEagerly.withoutStamps

    controller {
        TwoSided(
                voltageToDutyCycle(top, vBat),
                voltageToDutyCycle(bottom, vBat)
        )
    }
}

fun CollectorRollers.set(state: TwoSided<DutyCycle>) = newRoutine("Set") {
    controller { state }
}

fun CollectorSlider.set(target: Length, electrical: Electrical, tolerance: Length = 0.5.Inch) = newRoutine("Set") {

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

fun CollectorSlider.trackLine(lineScanner: LineScanner, electrical: Electrical) = newRoutine("Track line") {

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

fun CollectorSlider.set(target: DutyCycle) = newRoutine("Set") {
    controller { target }
}

fun CollectorSlider.manualOverride(operator: Operator) = newRoutine("Manual override") {
    val sliderPrecision by operator.sliderPrecision.readEagerly().withoutStamps
    controller { sliderPrecision }
}

fun Hook.set(target: HookPosition) = newRoutine("Set") {
    controller { target }
}

fun HookSlider.set(target: HookSliderState) = newRoutine("Set") {
    controller { target }
}