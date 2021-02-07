package com.lynbrookrobotics.nineteen.routines

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.electrical.*
import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.nineteen.subsystems.ElectricalSystemHardware
import com.lynbrookrobotics.nineteen.subsystems.collector.CollectorRollersComponent
import com.lynbrookrobotics.nineteen.subsystems.collector.HookComponent
import com.lynbrookrobotics.nineteen.subsystems.collector.HookPosition
import com.lynbrookrobotics.nineteen.subsystems.collector.hookslider.HookSliderComponent
import com.lynbrookrobotics.nineteen.subsystems.collector.hookslider.HookSliderState
import com.lynbrookrobotics.nineteen.subsystems.collector.pivot.CollectorPivotComponent
import com.lynbrookrobotics.nineteen.subsystems.collector.pivot.CollectorPivotState
import com.lynbrookrobotics.nineteen.subsystems.collector.slider.CollectorSliderComponent
import com.lynbrookrobotics.nineteen.subsystems.driver.OperatorHardware
import com.lynbrookrobotics.nineteen.subsystems.drivetrain.LineScannerHardware
import info.kunalsheth.units.generated.*

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

suspend fun CollectorSliderComponent.trackLine(lineScanner: LineScannerHardware, electrical: ElectricalSystemHardware) = startRoutine("Track line") {

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