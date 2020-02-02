package com.lynbrookrobotics.kapuchin.choreos

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.routines.*
import info.kunalsheth.units.generated.*
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext

suspend fun Subsystems.shooterTeleop() = startChoreo("Shooter Teleop") {
    val shoot by operator.shoot.readEagerly().withoutStamps
    val turretTurnRight by operator.turretTurnRight.readEagerly().withoutStamps
    val turretTurnLeft by operator.turretTurnLeft.readEagerly().withoutStamps

    choreography {
        runWhenever(
                { shoot } to choreography { shoot() },
                { turretTurnRight } to choreography { turretTurnRight() },
                { turretTurnLeft } to choreography { turretTurnLeft() }
        )
    }
}
suspend fun Subsystems.shoot() = supervisorScope() {
    try {
        launch { feederRoller?.spin(PercentOutput(feederRoller.hardware.escConfig, 30.Percent)) }
        launch {
            shooter?.set(PercentOutput(shooter.hardware.escConfig, 30.Percent))
        } finally {
            withContext(NonCancellable) {
            }
        }
    }
}
suspend fun Subsystems.turretTurnRight() = supervisorScope() {
    try {
        launch { turret?.spin(PercentOutput(shooter.hardware.escConfig, 30.Percent)) }

    } finally {
        withContext(NonCancellable)
    }
}
suspend fun Subsystems.turretTurnLeft() = supervisorScope() {
    try {
        launch {  turret?.spin(PercentOutput(shooter.hardware.escConfig, -Percent)) }
    } finally {
        withContext(NonCancellable)
    }
}


