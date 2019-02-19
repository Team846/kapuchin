package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.control.math.kinematics.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.collector.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.handoff.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.handoff.pivot.*
import info.kunalsheth.units.math.*
import info.kunalsheth.units.generated.*

suspend fun HandoffPivotComponent.to(target: Angle, tolerance: Angle = 5.Degree) = startRoutine("to") {

    val current by hardware.position.readOnTick.withoutStamps

    controller {
        with(hardware.conversions.native) {
            PositionOutput(
                    OffloadedPidGains(
                            kP = native(kP),
                            kI = 0.0,
                            kD = native(kD)
                    ), native(target)
            ).takeIf {
                current in target `±` tolerance
            }
        }
    }
}

suspend fun CollectorPivotComponent.to(target: CollectorPivotPosition) = startRoutine("to") {
    controller { target }
}

suspend fun CollectorRollersComponent.spin(target: DutyCycle) = startRoutine("spin") {
    controller { TwoSided(target) }
}

suspend fun CollectorSliderComponent.to(target: Length, tolerance: Length = 0.2.Inch) = startRoutine("to") {

    val current by hardware.position.readOnTick.withoutStamps
    val motionProfile = trapezoidalMotionProfile()

    controller {
        val error = target - current
    }
}

suspend fun HookComponent.to(target: HookPosition) = startRoutine("to") {
    controller { target }
}

suspend fun HookSliderComponent.to(target: HookSliderPosition) = startRoutine("to") {
    controller { target }
}

suspend fun HandoffRollersComponent.spin(target: DutyCycle) = startRoutine("spin") {
    controller { TwoSided(target) }
}

