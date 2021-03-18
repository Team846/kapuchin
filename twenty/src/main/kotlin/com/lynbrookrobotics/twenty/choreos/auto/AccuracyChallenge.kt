package com.lynbrookrobotics.twenty.choreos.auto

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.twenty.*
import com.lynbrookrobotics.twenty.routines.*
import com.lynbrookrobotics.twenty.subsystems.*
import com.lynbrookrobotics.twenty.subsystems.carousel.*
import com.lynbrookrobotics.twenty.subsystems.intake.*
import com.lynbrookrobotics.twenty.subsystems.shooter.*
import com.lynbrookrobotics.twenty.subsystems.shooter.FlashlightState.*
import com.lynbrookrobotics.twenty.subsystems.shooter.ShooterHoodState.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlinx.coroutines.launch

suspend fun Subsystems.zoneMoveThenShoot(zoneTarget: Pair<L, AngularVelocity>, hoodTarget: ShooterHoodState = Up) {

    if (flywheel == null || feederRoller == null) {
        log(Error) { "Need flywheel and feeder to run" }
        freeze()
    } else startChoreo("Finish Each Zone") {
        val reading by limelight.hardware.readings.readEagerly().withoutStamps
        val turretPos by turret!!.hardware.position.readEagerly().withoutStamps
        val accuracyChallenge by operator.accuracyChallenge.readEagerly().withoutStamps

        choreography {
            // Auto Aim Turret
            turret!!.set(turretPos - reading!!.tx.also { println(it) })

            // Wait to shoot until confirmation
            delayUntil { accuracyChallenge }

            // Set carousel initial
            val initialAngle = carousel.state.shootInitialAngle()
            if (initialAngle != null) {
                carousel.set(initialAngle)
            } else {
                log(Warning) { "No Balls" }
                withTimeout(2.Second) { rumble.set(TwoSided(100.Percent, 0.Percent)) }
            }

            // Spin up the flywheel and feeder roller
            launch { flywheel.set(zoneTarget.second) }
            launch { feederRoller.set(feederRoller.feedSpeed) }
            launch { shooterHood?.set(hoodTarget) }
            delay(0.5.Second)
            launch { carousel.set(carousel.fireAllDutycycle) }
            delay(0.5.Second)
        }
    }
}

suspend fun Subsystems.accuracyChallengeShoot() {

    if (flywheel == null || feederRoller == null) {
        log(Error) { "Need flywheel and feeder to run" }
        freeze()
    } else startChoreo("Accuracy Challenge") {
        val accGreenZone by driver.accGreenZone.readEagerly().withoutStamps
        val accYellowZone by driver.accYellowZone.readEagerly().withoutStamps
        val accBlueZone by driver.accBlueZone.readEagerly().withoutStamps
        val accRedZone by driver.accRedZone.readEagerly().withoutStamps

        choreography {
            // Shoot based on Zone
            runWhenever(
                { accGreenZone } to choreography { zoneMoveThenShoot(flywheel.greenZone, Down) },
                { accYellowZone } to choreography { zoneMoveThenShoot(flywheel.yellowZone, Down) },
                { accBlueZone } to choreography { zoneMoveThenShoot(flywheel.blueZone, Down) },
                { accRedZone } to choreography { zoneMoveThenShoot(flywheel.redZone, Down) },
            )
        }
    }
}