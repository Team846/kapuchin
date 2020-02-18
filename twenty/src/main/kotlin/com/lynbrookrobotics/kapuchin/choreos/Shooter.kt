package com.lynbrookrobotics.kapuchin.choreos

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.subsystems.shooter.ShooterHoodState.*
import info.kunalsheth.units.generated.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

suspend fun Subsystems.shooterTeleop() = startChoreo("Shooter Teleop") {

    val aim by operator.aim.readEagerly().withoutStamps
    val aimPreset by operator.aimPreset.readEagerly().withoutStamps
    val shoot by operator.shoot.readEagerly().withoutStamps
    val hoodUp by operator.hoodUp.readEagerly().withoutStamps

    val flywheelManual by operator.flywheelManual.readEagerly().withoutStamps
    val turretManual by operator.turretManual.readEagerly().withoutStamps

    choreography {
        launch { turret?.manualOverride(operator) }
        runWhenever(
                { aim } to choreography { aim() },
                { aimPreset } to choreography { },
                { shoot } to choreography { TODO("shoot") },
                { hoodUp } to choreography { shooterHood?.set(Up) },
                { !flywheelManual.isZero } to choreography { flywheel?.manualOverride(operator) },
                { !turretManual.isZero } to choreography { turret?.manualOverride(operator) }
        )

    }
}

//suspend fun Subsystems.aim() = if (flywheel != null && limelight != null && shooterHood != null) startChoreo("Aim") {
//
//    val reading by limelight.hardware.readings.readEagerly().withoutStamps
//
//    choreography {
//        turret?.trackTarget(limelight)
//    }
//
//} else println("Couldn't aim because of null subsystems")
