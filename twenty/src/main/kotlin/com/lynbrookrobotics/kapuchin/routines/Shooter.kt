package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.Field.innerGoalDepth
import com.lynbrookrobotics.kapuchin.control.electrical.*
import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.driver.*
import com.lynbrookrobotics.kapuchin.subsystems.drivetrain.*
import com.lynbrookrobotics.kapuchin.subsystems.limelight.*
import com.lynbrookrobotics.kapuchin.subsystems.shooter.*
import com.lynbrookrobotics.kapuchin.subsystems.shooter.Goal.*
import com.lynbrookrobotics.kapuchin.subsystems.shooter.flywheel.*
import com.lynbrookrobotics.kapuchin.subsystems.shooter.turret.*
import com.lynbrookrobotics.kapuchin.timing.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

suspend fun FlywheelComponent.set(target: AngularVelocity) = startRoutine("Set") {
    controller {
        VelocityOutput(hardware.escConfig, velocityGains, hardware.conversions.encoder.native(target))
    }
}

suspend fun FlywheelComponent.manualOverride(operator: OperatorHardware) = startRoutine("Manual Override") {
    val precision by operator.flywheelManual.readOnTick.withoutStamps

    controller {
        val target = maxSpeed * precision
        VelocityOutput(hardware.escConfig, velocityGains, hardware.conversions.encoder.native(target))
    }
}

suspend fun FeederRollerComponent.set(target: AngularVelocity) = startRoutine("Set") {
    controller {
        VelocityOutput(hardware.escConfig, velocityGains, hardware.conversions.native(target))
    }
}

suspend fun TurretComponent.set(target: Angle, tolerance: Angle = 2.Degree) = startRoutine("Set") {
    val current by hardware.position.readOnTick.withoutStamps

    controller {
        PositionOutput(hardware.escConfig, positionGains, hardware.conversions.encoder.native(target))
                .takeUnless { target - current in `±`(tolerance) }
    }
}

suspend fun TurretComponent.manualOverride(operator: OperatorHardware) = startRoutine("Manual Override") {
    val precision by operator.turretManual.readOnTick.withoutStamps

    controller { PercentOutput(hardware.escConfig, precision) }
}

suspend fun TurretComponent.trackTarget(
        limelight: LimelightComponent, flywheel: FlywheelComponent, drivetrain: DrivetrainComponent,
        goal: Goal, tolerance: Angle? = null
) = startRoutine("Track Target") {

    val reading by limelight.hardware.readings.readOnTick.withoutStamps
    val robotPosition by drivetrain.hardware.position.readEagerly().withoutStamps
    val current by hardware.position.readOnTick.withoutStamps

    controller {
        reading?.let { snapshot ->
            val target = when (goal) {
                Outer -> current + snapshot.tx
                Inner -> with(limelight.hardware.conversions) {
                    val llTarget = goalPositions(snapshot, robotPosition.bearing)
                    val horizontalOffset = innerGoalOffsets(llTarget, flywheel.shooterHeight).first
                    val dtheta = atan(innerGoalDepth / horizontalOffset) - (90.Degree - (snapshot.tx + robotPosition.bearing))
                    current + snapshot.tx - dtheta
                }
            }

            PositionOutput(
                    hardware.escConfig, positionGains, hardware.conversions.encoder.native(target)
            ).takeUnless { snapshot.tx.abs < tolerance ?: -1.Degree }
        } ?: run {
            log(Debug) { "Lost sight of target!" }
            null
        }
    }
}

suspend fun TurretComponent.fieldOrientedPosition(drivetrain: DrivetrainComponent) = startRoutine("Field Oriented Position") {
    val drivetrainPosition by drivetrain.hardware.position.readEagerly.withoutStamps

    // Initial field oriented bearing of the turret
    val initial = drivetrainPosition.bearing `coterminal +` hardware.position.optimizedRead(currentTime, 0.Second).y

    controller {
        val target = initial `coterminal -` drivetrainPosition.bearing
        PositionOutput(hardware.escConfig, positionGains, hardware.conversions.encoder.native(target))
                .takeIf { target in hardware.conversions.min..hardware.conversions.max }
    }
}

suspend fun TurretComponent.rezero(electrical: ElectricalSystemHardware) = startRoutine("Re-zero") {
    val vBat by electrical.batteryVoltage.readEagerly.withoutStamps

    hardware.isZeroed = false
    controller {
        PercentOutput(hardware.escConfig, voltageToDutyCycle(safeSpeed, vBat)).takeUnless { hardware.isZeroed }
    }
}

suspend fun ShooterHoodComponent.set(target: ShooterHoodState) = startRoutine("Set") {
    controller { target }
}
