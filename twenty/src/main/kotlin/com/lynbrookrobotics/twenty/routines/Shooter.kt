package com.lynbrookrobotics.twenty.routines

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.electrical.*
import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.twenty.subsystems.ElectricalSystemHardware
import com.lynbrookrobotics.twenty.subsystems.driver.OperatorHardware
import com.lynbrookrobotics.twenty.subsystems.drivetrain.DrivetrainComponent
import com.lynbrookrobotics.twenty.subsystems.limelight.LimelightComponent
import com.lynbrookrobotics.twenty.subsystems.shooter.*
import com.lynbrookrobotics.twenty.subsystems.shooter.flywheel.FlywheelComponent
import com.lynbrookrobotics.twenty.subsystems.shooter.turret.TurretComponent
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

suspend fun FlywheelComponent.set(target: AngularVelocity) = startRoutine("Set Omega") {
    controller {
        VelocityOutput(hardware.escConfig, velocityGains, hardware.conversions.encoder.native(target))
    }
}

suspend fun FlywheelComponent.set(target: DutyCycle) = startRoutine("Set Duty Cycle") {
    controller {
        PercentOutput(hardware.escConfig, target)
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
        PositionOutput(hardware.escConfig, positionGains, hardware.conversions.encoder.native(target))
            .takeUnless { target - current in `±`(tolerance) }
    }
}

suspend fun TurretComponent.manualOverride(operator: OperatorHardware) = startRoutine("Manual Override") {
    val precision by operator.turretManual.readOnTick.withoutStamps
    controller { PercentOutput(hardware.escConfig, precision) }
}

suspend fun TurretComponent.manualPrecisionOverride(operator: OperatorHardware) =
    startRoutine("Manual Precision Override") {
        val precision by operator.turretPrecisionManual.readOnTick.withoutStamps
        controller { PercentOutput(hardware.escConfig, precision) }
    }

suspend fun TurretComponent.trackTarget(limelight: LimelightComponent) = startRoutine("Track Target") {

    val reading by limelight.hardware.readings.readOnTick.withoutStamps
    val current by hardware.position.readOnTick.withoutStamps

    controller {
        reading?.let { snapshot ->
            val target = current + snapshot.tx + limelight.hardware.conversions.mountingBearing

            PositionOutput(
                hardware.escConfig, positionGains, hardware.conversions.encoder.native(target)
            )
        } ?: run {
            log(Debug) { "Lost sight of target!" }
            PercentOutput(hardware.escConfig, 0.Percent)
        }
    }
}

suspend fun TurretComponent.fieldOriented(drivetrain: DrivetrainComponent, limelight: LimelightComponent) {
    startRoutine("Field Oriented Routine") {
        val reading by limelight.hardware.readings.readEagerly.withoutStamps
        val drivetrainPosition by drivetrain.hardware.position.readEagerly.withoutStamps
        val bearing = drivetrainPosition.bearing

        if(reading != null) lastLimelightPos = reading

        controller {
            if (lastLimelightPos != null) {
                log(Debug) {"Target detecting ... Aiming now"}
                val pitch by drivetrain.hardware.pitch.readEagerly().withoutStamps
//                val position = limelight.hardware.conversions.outerGoalPosition(lastLimelightPos!!, bearing, pitch)
                val position = Position(5.Foot, 5.Foot, 0.Degree)
                val angle = atan2(position.x - drivetrainPosition.x, position.y - drivetrainPosition.y)
                val target = -(angle `coterminal +` drivetrainPosition.bearing)
                if (target in hardware.conversions.min..hardware.conversions.max) PositionOutput(hardware.escConfig,
                    positionGains,
                    hardware.conversions.encoder.native(target))
                else{
                    log(Debug) {"No target detected ..."}
                    PercentOutput(hardware.escConfig, 0.Percent)
                }
            } else{
                log(Debug) {"No target detected ..."}
                PercentOutput(hardware.escConfig, 0.Percent)
            }
        }
    }
}


suspend fun TurretComponent.fieldOrientedAngle(drivetrain: DrivetrainComponent, toTurretPosition: Angle? = null) =
    startRoutine("Field Oriented Position") {
        val drivetrainPosition by drivetrain.hardware.position.readEagerly.withoutStamps
        val turretPosition by hardware.position.readEagerly.withoutStamps

        // Initial field oriented bearing of the turret
        val initial = drivetrainPosition.bearing `coterminal +` (toTurretPosition ?: turretPosition)

        controller {
            val target = initial `coterminal -` drivetrainPosition.bearing

            if (target in hardware.conversions.min..hardware.conversions.max)
                PositionOutput(hardware.escConfig, positionGains, hardware.conversions.encoder.native(target))
            else
                PercentOutput(hardware.escConfig, 0.Percent)
        }
    }

suspend fun TurretComponent.fieldOrientedPosition(drivetrain: DrivetrainComponent, targetPos: UomVector<Length>) =
    startRoutine("Track Position Field Oriented") {
        val drivetrainPosition by drivetrain.hardware.position.readEagerly.withoutStamps

        controller {
            val angle = atan2(targetPos.x - drivetrainPosition.x, targetPos.y - drivetrainPosition.y)
            val target = angle `coterminal -` drivetrainPosition.bearing

            if (target in hardware.conversions.min..hardware.conversions.max)
                PositionOutput(hardware.escConfig, positionGains, hardware.conversions.encoder.native(target))
            else
                PercentOutput(hardware.escConfig, 0.Percent)
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
