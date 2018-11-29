package com.lynbrookrobotics.kapuchin.routines.teleop

import com.lynbrookrobotics.kapuchin.logging.Level.Warning
import com.lynbrookrobotics.kapuchin.logging.log
import com.lynbrookrobotics.kapuchin.subsystems.DriverHardware
import com.lynbrookrobotics.kapuchin.subsystems.LiftComponent
import com.lynbrookrobotics.kapuchin.subsystems.climber.ForksComponent
import com.lynbrookrobotics.kapuchin.subsystems.climber.HooksComponent
import com.lynbrookrobotics.kapuchin.subsystems.climber.WinchComponent
import info.kunalsheth.units.generated.Volt
import info.kunalsheth.units.generated.`±`

suspend fun HooksComponent.teleop(driver: DriverHardware, lift: LiftComponent) = startRoutine("teleop") {
    var state = false
    var lastIsTriggered = false

    val isTriggered by driver.deployHooks.readEagerly.withoutStamps
    val liftPosition by lift.hardware.position.readEagerly.withoutStamps
    val liftSafeThreshold = lift.collectHeight `±` lift.positionTolerance

    controller {
        if (isTriggered && !lastIsTriggered) state = !state
        lastIsTriggered = isTriggered

        if (state && liftPosition !in liftSafeThreshold) {
            log(Warning) { "Cannot deploy hooks until lift is lowered" }
            false
        } else state
    }
}

suspend fun WinchComponent.teleop(driver: DriverHardware) = startRoutine("teleop") {
    val isClimbing by driver.climb.readEagerly.withoutStamps

    val manualOverride by driver.manualOverride.readEagerly.withoutStamps
    val overrideWinch by driver.manualClimb.readEagerly.withoutStamps

    controller {
        when {
            overrideWinch -> hardware.operatingVoltage * manualOverride
            isClimbing -> climbStrength
            else -> 0.Volt
        }
    }
}

suspend fun ForksComponent.teleop(driver: DriverHardware) = startRoutine("teleop") {
    val isTriggered by driver.deployForks.readEagerly.withoutStamps
    controller { isTriggered }
}