package com.lynbrookrobotics.twenty.choreos.auto

import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.twenty.Subsystems
import com.lynbrookrobotics.twenty.choreos.delayUntilFeederAndFlywheel
import com.lynbrookrobotics.twenty.choreos.visionAimTurret
import com.lynbrookrobotics.twenty.routines.*
import com.lynbrookrobotics.twenty.subsystems.carousel.CarouselSlot
import com.lynbrookrobotics.twenty.subsystems.shooter.ShooterHoodState
import info.kunalsheth.units.generated.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

object Auto : Named by Named("Auto") {
    val flywheelLinePreset by pref(5000, Rpm)
    val fireTimeout by pref(9, Second)

    val linePathConfig by autoPathConfigPref("", defaultReverse = true)
    val lineDist by pref(4, Foot)
}

suspend fun Subsystems.`auto line`() = startChoreo("Auto L") {
    choreography {
        launch { zeroSubsystems() }
        drivetrain.followTrajectory(fastAsFuckLine(Auto.lineDist, Auto.linePathConfig), Auto.linePathConfig)
    }
}

suspend fun Subsystems.`auto I shoot line`() = startChoreo("Auto I shoot L") {
    choreography {
        withTimeout(Auto.fireTimeout) { autoFire() }
        launch { zeroSubsystems() }
        drivetrain.followTrajectory(fastAsFuckLine(Auto.lineDist, Auto.linePathConfig), Auto.linePathConfig)
    }
}

private suspend fun Subsystems.autoFire() = startChoreo("Auto fire") {

    val carouselPosition by carousel.hardware.position.readEagerly().withoutStamps

    choreography {
        launch { visionAimTurret() }

        launch { flywheel?.set(Auto.flywheelLinePreset) }
        launch { feederRoller?.set(feederRoller.feedSpeed) }
        launch { shooterHood?.set(ShooterHoodState.Up) }

        delayUntilFeederAndFlywheel(Auto.flywheelLinePreset)

        withTimeout(3.Second) { carousel.set(carousel.fireAllDutycycle) }
    }

}

private suspend fun Subsystems.zeroSubsystems() = coroutineScope {
    carousel.state.clear()

    val j = launch { turret?.rezero(electrical) }
    carousel.rezero()
    j.join()
}