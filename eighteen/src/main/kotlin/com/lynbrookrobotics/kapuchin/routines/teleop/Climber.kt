package com.lynbrookrobotics.kapuchin.routines.teleop

import com.lynbrookrobotics.kapuchin.control.withToleranceOf
import com.lynbrookrobotics.kapuchin.logging.Level.Warning
import com.lynbrookrobotics.kapuchin.logging.log
import com.lynbrookrobotics.kapuchin.subsystems.DriverHardware
import com.lynbrookrobotics.kapuchin.subsystems.LiftComponent
import com.lynbrookrobotics.kapuchin.subsystems.climber.ForksComponent
import com.lynbrookrobotics.kapuchin.subsystems.climber.HooksComponent
import com.lynbrookrobotics.kapuchin.subsystems.climber.WinchComponent
import info.kunalsheth.units.generated.Volt

suspend fun HooksComponent.teleop(driver: DriverHardware, lift: LiftComponent) {
    var state = false
    val isTriggered by driver.deployHooks.readEagerly.withoutStamps
    val liftPosition by lift.hardware.position.readEagerly.withoutStamps
    val liftSafeThreshold = lift.collectHeight withToleranceOf lift.positionTolerance

    runRoutine("Teleop") {
        if (isTriggered) state = !state
        if (state && liftPosition !in liftSafeThreshold) {
            log(Warning) { "Cannot deploy hooks until lift is lowered" }
            false
        } else state
    }
}

suspend fun WinchComponent.teleop(driver: DriverHardware) {
    val isClimbing by driver.climb.readEagerly.withoutStamps

    val manualOverride by driver.manualOverride.readEagerly.withoutStamps
    val overrideWinch by driver.manualClimb.readEagerly.withoutStamps

    runRoutine("Teleop") {
        when {
            overrideWinch -> hardware.operatingVoltage * manualOverride
            isClimbing -> climbStrength
            else -> 0.Volt
        }
    }
}

suspend fun ForksComponent.teleop(driver: DriverHardware) {
    val isTriggered by driver.deployForks.readEagerly.withoutStamps
    runRoutine("Teleop") { isTriggered }
}