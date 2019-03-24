package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.electrical.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.driver.*
import com.lynbrookrobotics.kapuchin.subsystems.drivetrain.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.collector.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.collector.hookslider.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.collector.pivot.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.collector.slider.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.handoff.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.handoff.pivot.*
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

suspend fun CollectorSliderComponent.trackLine(tolerance: Length, lineScanner: LineScannerHardware, electrical: ElectricalSystemHardware) = startRoutine("Track line") {

    val target by lineScanner.linePosition.readOnTick.withoutStamps
    val current by hardware.position.readOnTick.withoutStamps
    val vBat by electrical.batteryVoltage.readEagerly.withoutStamps

    controller {
        val error = (target ?: 0.Inch) - current
        val voltage = kP * error

        voltageToDutyCycle(voltage, vBat).takeUnless {
            error.abs < tolerance
        }
    }
}

suspend fun CollectorSliderComponent.set(target: DutyCycle) = startRoutine("Manual Override") {
    controller { target }
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

suspend fun HandoffPivotComponent.set(target: Angle, tolerance: Angle = 5.Degree) = startRoutine("Set Angle") {

    val current by hardware.position.readOnTick.withoutStamps

    controller {
        with(hardware.conversions.native) {
            PositionOutput(
                    OffloadedPidGains(
                            kP = native(kP),
                            kI = 0.0,
                            kD = native(kD)
                    ), native(target)
            ).takeUnless {
                (target - current).abs < tolerance
            }
        }
    }
}

suspend fun HandoffPivotComponent.set(speed: DutyCycle) = startRoutine("Set Duty Cycle") {
    controller { PercentOutput(speed) }
}

suspend fun HandoffRollersComponent.spin(target: DutyCycle) = startRoutine("Spin") {
    controller { TwoSided(target) }
}

suspend fun VelcroPivotComponent.set(target: VelcroPivotPosition) = startRoutine("Set") {
    controller { target }
}