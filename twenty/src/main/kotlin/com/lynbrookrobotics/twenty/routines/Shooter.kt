package com.lynbrookrobotics.twenty.routines

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
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

suspend fun TurretComponent.trackTarget(drivetrain: DrivetrainComponent, limelight: LimelightComponent) =
    startRoutine("Track Target") {
        val reading by limelight.hardware.readings.readOnTick.withoutStamps
        val drivetrainPosition by drivetrain.hardware.position.readEagerly.withoutStamps
        val turretPosition by hardware.position.readOnTick.withoutStamps

        var lastAngle: Angle? = null

        controller {
            // TODO sid
            // aim for inner if within skew tolerance
            reading?.let { snapshot ->
                val target = turretPosition - snapshot.tx + limelight.hardware.conversions.mountingBearing
                lastAngle = drivetrainPosition.bearing `coterminal +` target

                PositionOutput(
                    hardware.escConfig, positionGains, hardware.conversions.encoder.native(target)
                )
            } ?: lastAngle?.let { lastAngle ->
                val target = lastAngle `coterminal -` drivetrainPosition.bearing
                PositionOutput(hardware.escConfig, positionGains, hardware.conversions.encoder.native(target))
            } ?: PercentOutput(hardware.escConfig, 0.Percent)
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

//suspend fun TurretComponent.rezero(electrical: ElectricalSystemHardware) = startRoutine("Re-zero") {
//    val vBat by electrical.batteryVoltage.readEagerly.withoutStamps
//
//    hardware.isZeroed = false
//    controller {
//        PercentOutput(hardware.escConfig, voltageToDutyCycle(safeSpeed, vBat)).takeUnless { hardware.isZeroed }
//    }
//}

suspend fun ShooterHoodComponent.set(target: ShooterHoodState) = startRoutine("Set") {
    controller { target }
}
