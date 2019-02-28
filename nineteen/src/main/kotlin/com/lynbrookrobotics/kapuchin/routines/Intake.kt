package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.electrical.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.drivetrain.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.collector.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.handoff.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.handoff.pivot.*
import com.lynbrookrobotics.kapuchin.control.electrical.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.collector.hookslider.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.collector.pivot.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.collector.slider.*
import info.kunalsheth.units.math.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

suspend fun CollectorPivotComponent.set(target: CollectorPivotState) = startRoutine("Set") {
    controller { target }
}

suspend fun CollectorRollersComponent.spin(electrical: ElectricalSystemHardware, bottom: V, top: V = bottom) = startRoutine("Spin") {
    val vBat by electrical.batteryVoltage.readEagerly.withoutStamps

    controller {
        TwoSided(
                voltageToDutyCycle(bottom, vBat),
                voltageToDutyCycle(top, vBat)
        )
    }
}

suspend fun CollectorSliderComponent.set(target: Length, electrical: ElectricalSystemHardware, tolerance: Length = 0.2.Inch) = startRoutine("Set") {

    val current by hardware.position.readOnTick.withoutStamps
    val vBat by electrical.batteryVoltage.readEagerly.withoutStamps

    controller {
        val error = target - current
        val voltage = kP * error

        voltageToDutyCycle(voltage, vBat).takeIf {
            current in target `±` tolerance
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

        voltageToDutyCycle(voltage, vBat).takeIf {
            error in 0.Inch `±` tolerance
        }
    }
}

suspend fun CollectorSliderComponent.set(target: DutyCycle) = startRoutine("Zero") {
    controller { target }
}

suspend fun CollectorSliderComponent.zero() = startChoreo("Zero") {

    val atZero by hardware.atZero.readEagerly().withoutStamps

    choreography {
        runWhile({ !atZero }) { set(-20.Percent) }
        hardware.zero()
    }
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
                current in target `±` tolerance
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