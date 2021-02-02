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

suspend fun FlywheelComponent.set(target: AngularVelocity) = startRoutine("Set Omega") {
    val current by hardware.speed.readOnTick.withoutStamps
    controller {
        println("$currentTime \t $current")
        VelocityOutput(hardware.escConfig, velocityGains, hardware.conversions.encoder.native(target))
    }
}

suspend fun FlywheelComponent.set(target: DutyCycle) = startRoutine("Set Duty Cycle") {
    controller {
        PercentOutput(hardware.escConfig, target)
    }
}

suspend fun FlywheelComponent.manualOverride(operator: OperatorHardware) = startRoutine("Manual Override") {
    val precision by operator.flywheelManual.readOnTick.withoutStamps

    controller {
        precision?.let {
            val target = it * maxSpeed
            VelocityOutput(hardware.escConfig, velocityGains, hardware.conversions.encoder.native(target))
        }
    }
}

suspend fun FeederRollerComponent.set(target: AngularVelocity) = startRoutine("Set Omega") {
    controller {
        VelocityOutput(hardware.escConfig, velocityGains, hardware.conversions.native(target))
    }
}

suspend fun FeederRollerComponent.set(target: DutyCycle) = startRoutine("Set Duty Cycle") {
    controller {
        PercentOutput(hardware.escConfig, target)
    }
}

suspend fun TurretComponent.set(target: Angle, tolerance: Angle = 0.2.Degree) = startRoutine("Set") {
    val current by hardware.position.readOnTick.withoutStamps

    controller {
        println("$currentTime \t $current.Degree")
        PositionOutput(hardware.escConfig, positionGains, hardware.conversions.encoder.native(target))
            .takeUnless { target - current in `±`(tolerance) }
    }
}

suspend fun TurretComponent.manualOverride(operator: OperatorHardware) = startRoutine("Manual Override") {
    val precision by operator.turretManual.readOnTick.withoutStamps
    controller { PercentOutput(hardware.escConfig, precision) }
}

@Deprecated("Do not use. Doesn't work accross limelight pipeline shifts.")
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
                Outer -> current + snapshot.tx + limelight.hardware.conversions.mountingBearing
                Inner -> with(limelight.hardware.conversions) {
                    val llTarget = goalPositions(snapshot, robotPosition.bearing)
                    val horizontalOffset = innerGoalOffsets(llTarget, flywheel.shooterHeight).first
                    val dtheta =
                        atan(innerGoalDepth / horizontalOffset) - (90.Degree - (snapshot.tx + limelight.hardware.conversions.mountingBearing + robotPosition.bearing))
                    current + snapshot.tx + limelight.hardware.conversions.mountingBearing - dtheta
                }
            }

            PositionOutput(
                hardware.escConfig, positionGains, hardware.conversions.encoder.native(target)
            ).takeUnless { (snapshot.tx + limelight.hardware.conversions.mountingBearing).abs < tolerance ?: -1.Degree }
        } ?: run {
            log(Debug) { "Lost sight of target!" }
            null
        }
    }
}

suspend fun TurretComponent.fieldOrientedPosition(drivetrain: DrivetrainComponent, toTurretPosition: Angle? = null) =
    startRoutine("Field Oriented Position") {
        val drivetrainPosition by drivetrain.hardware.position.readEagerly.withoutStamps
        val turretPosition by hardware.position.readEagerly.withoutStamps

        // Initial field oriented bearing of the turret
        val initial = drivetrainPosition.bearing `coterminal +` (toTurretPosition ?: turretPosition)

        controller {
            val target = initial `coterminal -` drivetrainPosition.bearing
            PositionOutput(hardware.escConfig, positionGains, hardware.conversions.encoder.native(target))
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
