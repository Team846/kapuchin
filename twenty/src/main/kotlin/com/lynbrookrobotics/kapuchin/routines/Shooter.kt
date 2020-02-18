package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.subsystems.driver.*
import com.lynbrookrobotics.kapuchin.subsystems.limelight.*
import com.lynbrookrobotics.kapuchin.subsystems.shooter.*
import com.lynbrookrobotics.kapuchin.subsystems.shooter.flywheel.*
import com.lynbrookrobotics.kapuchin.subsystems.shooter.turret.*
import info.kunalsheth.units.generated.*

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

suspend fun TurretComponent.trackTarget(limelight: LimelightComponent, tolerance: Angle = 2.Degree) = startRoutine("Track Target") {
    val reading by limelight.hardware.readings.readOnTick.withoutStamps
    val current by hardware.position.readOnTick.withoutStamps

    controller {
        reading?.let { r ->
            val target = current + r.tx
            PositionOutput(hardware.escConfig, positionGains, hardware.conversions.encoder.native(target))
                    .takeUnless { r.tx in `±`(tolerance) }
        }
    }
}

suspend fun ShooterHoodComponent.set(target: ShooterHoodState) = startRoutine("Set") {
    controller { target }
}
