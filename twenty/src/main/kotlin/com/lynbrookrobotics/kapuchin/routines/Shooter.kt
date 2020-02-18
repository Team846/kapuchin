package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.subsystems.shooter.*
import com.lynbrookrobotics.kapuchin.subsystems.shooter.flywheel.*
import com.lynbrookrobotics.kapuchin.subsystems.shooter.turret.*
import info.kunalsheth.units.generated.*

suspend fun FlywheelComponent.set(target: AngularVelocity, tolerance: AngularVelocity = 30.Rpm) = startRoutine("Set") {

    val current by hardware.speed.readOnTick.withoutStamps

    controller {
        VelocityOutput(hardware.escConfig, velocityGains, hardware.conversions.encoder.native(target))
                .takeUnless { (target - current).abs < tolerance }
    }
}

suspend fun TurretComponent.set(target: Angle, tolerance: Angle = 2.Degree) = startRoutine("Set") {

    val current by hardware.position.readOnTick.withoutStamps

    controller {
        PositionOutput(hardware.escConfig, positionGains, hardware.conversions.encoder.native(target))
                .takeUnless { (target - current).abs < tolerance }
    }
}

suspend fun ShooterHoodComponent.set(target: ShooterHoodState) = startRoutine("Set") {
    controller { target }
}

