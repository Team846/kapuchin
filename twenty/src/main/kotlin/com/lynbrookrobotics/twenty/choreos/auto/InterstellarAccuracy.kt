package com.lynbrookrobotics.twenty.choreos.auto

import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.twenty.Auto.InterstellarAccuracy
import com.lynbrookrobotics.twenty.Subsystems
import com.lynbrookrobotics.twenty.choreos.intakeBalls
import com.lynbrookrobotics.twenty.choreos.shootAll
import com.lynbrookrobotics.twenty.choreos.spinUpShooter
import com.lynbrookrobotics.twenty.routines.manualOverride
import com.lynbrookrobotics.twenty.routines.set
import com.lynbrookrobotics.twenty.subsystems.shooter.ShooterHoodState.Up
import kotlinx.coroutines.launch

suspend fun Subsystems.interstellarAccuracyTeleop() = startChoreo("Interstellar Accuracy Teleop") {

    val intakeBalls by driver.intakeBalls.readEagerly().withoutStamps
    val unjamBalls by driver.unjamBalls.readEagerly().withoutStamps

    val shoot by operator.shoot.readEagerly().withoutStamps

    val ball0 by operator.ball0.readEagerly().withoutStamps
    val ball1 by operator.ball1.readEagerly().withoutStamps
    val ball2 by operator.ball2.readEagerly().withoutStamps
    val ball3 by operator.ball3.readEagerly().withoutStamps

    val zone1 by operator.zone1.readEagerly().withoutStamps
    val zone2 by operator.zone2.readEagerly().withoutStamps
    val zone3 by operator.zone3.readEagerly().withoutStamps
    val zone4 by operator.zone4.readEagerly().withoutStamps

    choreography {
        if (turret != null && !turret.hardware.isZeroed) launch {
            log(Debug) { "Rezeroing turret" }
            turret.hardware.isZeroed = true
//            turret.rezero(electrical)
        }

        launch { turret?.manualOverride(operator) ?: freeze() }

        runWhenever(
            { intakeBalls } to {
                carousel.state.clear()
                intakeBalls()
            },
            { unjamBalls } to { intakeRollers?.set(intakeRollers.pukeSpeed) ?: freeze() },

            { shoot } to { shootAll() },
            { ball0 } to {
                carousel.state.clear()
            },
            { ball1 } to {
                carousel.state.clear()
                carousel.state.push(1)
            },
            { ball2 } to {
                carousel.state.clear()
                carousel.state.push(2)
            },
            { ball3 } to {
                carousel.state.clear()
                carousel.state.push(3)
            },

            { zone1 } to {
                carousel.state.clear()
                carousel.state.push(3)
                flywheel?.let { spinUpShooter(InterstellarAccuracy.zone1) } ?: freeze()
            },
            { zone2 } to {
                carousel.state.clear()
                carousel.state.push(3)
                flywheel?.let { spinUpShooter(InterstellarAccuracy.zone2) } ?: freeze()
            },
            { zone3 } to {
                carousel.state.clear()
                carousel.state.push(3)
                flywheel?.let { spinUpShooter(InterstellarAccuracy.zone3, hoodTarget = Up) } ?: freeze()
            },
            { zone4 } to {
                carousel.state.clear()
                carousel.state.push(3)
                flywheel?.let { spinUpShooter(InterstellarAccuracy.zone4, hoodTarget = Up) } ?: freeze()
            },
        )
    }
}