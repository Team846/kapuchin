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
    val shootOverride by operator.shootOverride.readEagerly().withoutStamps

    val shooterHoodManual by operator.shooterHoodManual.readEagerly().withoutStamps

    choreography {
        launch { turret?.manualOverride(operator) }
        runWhenever(
                { aim } to choreography { aim() },
                { aimPreset } to choreography { },
                { shoot } to choreography { TODO("shoot") },
                { shootOverride } to choreography { },
                { shooterHoodManual } to choreography { hoodUp() }
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
