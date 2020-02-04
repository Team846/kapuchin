package com.lynbrookrobotics.kapuchin.choreos

import com.lynbrookrobotics.kapuchin.*
<<<<<<< HEAD
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.subsystems.limelight.*
import com.lynbrookrobotics.kapuchin.subsystems.shooter.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlin.math.sqrt

suspend fun Subsystems.aimAndShootPowerCell() = startChoreo("Shoot power cell") {

    fun requiredVelocities(
            flywheel: FlywheelComponent, hood: HoodComponent,
            hoodState: HoodState
    ): Pair<Velocity, AngularVelocity> {
        val launchA = if (hoodState == HoodState.Down) hood.launchAngles.first else hood.launchAngles.second


        val dist = 0.Foot // TODO
        val deltaHeight = flywheel.targetHeight - flywheel.height

        val expression = ((dist * 1.EarthGravity) / (((dist * tan(launchA)) - deltaHeight) * cos(launchA) * cos(launchA))) * dist
        val ballVelocity = Velocity(sqrt(expression.siValue / 2))


        val flywheelOmega = with(flywheel) {
            AngularVelocity(((momentFactor * ballMass + (2 * momentOfInertia / (rollerRadius * rollerRadius))) * ballVelocity * rollerRadius / momentOfInertia).siValue)
        }

        return ballVelocity to flywheelOmega
    }

    fun targetEntryAngle(hood: HoodComponent, hoodState: HoodState, ballVelocity: Velocity): Angle {
        val launchA = if (hoodState == HoodState.Down) hood.launchAngles.first else hood.launchAngles.second
        val dist = /*distanceToTarget(target)*/0.Foot // TODO
        val slope = ((-1 * dist * 1.EarthGravity) / (ballVelocity * ballVelocity * cos(launchA) * cos(launchA))) + tan(launchA)
        return atan(slope).abs
    }

    fun shotState(
            flywheel: FlywheelComponent, hood: HoodComponent,
            hoodState: HoodState, target: Position
    ): Pair<AngularVelocity, Angle> {
        val targetHeight = flywheel.targetHeight - flywheel.height
        val (ballVelocity, flywheelOmega) = requiredVelocities(flywheel, hood, hoodState)
        val entryAngle = targetEntryAngle(hood, hoodState, ballVelocity)
        return flywheelOmega to entryAngle
    }

    fun offsets(flywheel: FlywheelComponent): Pair<Length, Length> { // Pair(Horizontal, Vertical)
        val skew = 0.Degree // TODO
        val dist = 0.Inch // TODO

        val horizontal =  flywheel.outerInnerDiff * tan(skew)
        val vertical = with(flywheel) {
            (outerInnerDiff * (targetHeight - height)) / (dist * cos(skew))
        }
        return horizontal to vertical
    }

    fun entryAngleLimits(flywheel: FlywheelComponent): Pair<Angle, Angle> {
        val downward = atan(((flywheel.hexagonHeight / 2) + offsets(flywheel).second) / flywheel.outerInnerDiff) // approach from below
        val upward = 90.Degree - atan(flywheel.outerInnerDiff / ((flywheel.hexagonHeight / 2) - offsets(flywheel).second)) // approach from upward
        return downward to upward
    }

    fun innerGoalPossible(flywheel: FlywheelComponent): Boolean {
        val horizontal  = offsets(flywheel).first
        val vertical = offsets(flywheel).second
        return ((horizontal * horizontal) + (vertical * vertical)) < flywheel.boundingCircleRadius * flywheel.boundingCircleRadius
    }

    choreography {
        // TODO get target, do nothing if null
        if (limelight == null) return@choreography
        val target = DetectedTarget(Position(0.Foot, 0.Foot, 0.Degree), Position(0.Foot, 0.Foot, 0.Degree))
        val targetHeight = flywheel?.targetHeight

        if (flywheel != null && hood != null) {
            // TODO check if target is physically impossible to shoot to
            val downInner = target.inner
                    ?.let { shotState(flywheel, hood, HoodState.Down, it) }
                    ?.let { it.first < flywheel.maxOmega
                            && it.second in entryAngleLimits(flywheel).first..entryAngleLimits(flywheel).second
                            && innerGoalPossible(flywheel) }
                    ?: false
            val downOuter = target.outer
                    ?.let { shotState(flywheel, hood, HoodState.Down, it) }
                    ?.let { it.second.abs < flywheel.outerGoalEntryTolerance && it.first < flywheel.maxOmega }
                    ?: false
            val upInner = target.inner
                    ?.let { shotState(flywheel, hood, HoodState.Up, it) }
                    ?.let { it.first < flywheel.maxOmega
                            && it.second in entryAngleLimits(flywheel).first..entryAngleLimits(flywheel).second
                            && innerGoalPossible(flywheel) }
                    ?: false
            val upOuter = target.outer
                    ?.let { shotState(flywheel, hood, HoodState.Up, it) }
                    ?.let { it.second.abs < flywheel.outerGoalEntryTolerance && it.first < flywheel.maxOmega }
                    ?: false

            /*
            DownInner | UpInner | DownOuter | UpOuter || Hood |  Goal |
            ----------|---------|-----------|---------||------|-------|
                n     |    n    |     n     |    n    ||      |       |
                n     |    n    |     n     |    y    ||  up  | outer |
                n     |    n    |     y     |    n    || down | outer |
                n     |    n    |     y     |    y    ||  <∠  | outer | choose better entry angle
                n     |    y    |     n     |    n    ||      |       |
                n     |    y    |     n     |    y    ||  up  | inner |
                n     |    y    |     y     |    n    ||      |       |
                n     |    y    |     y     |    y    ||  up  | inner |
                y     |    n    |     n     |    n    ||      |       |
                y     |    n    |     n     |    y    ||      |       |
                y     |    n    |     y     |    n    || down | inner |
                y     |    n    |     y     |    y    || down | inner |
                y     |    y    |     n     |    n    ||      |       |
                y     |    y    |     n     |    y    ||      |       |
                y     |    y    |     y     |    n    ||      |       |
                y     |    y    |     y     |    y    ||  <∠  | inner | choose better entry angle
             */

        }

        /*
         * Launch flywheel spinup
         * Launch turret position set
         * Launch hood state set
         * Wait until flywheel and turret are set
         * Spin feeder roller
         *
         * If shooting multiple
         */
    }

}
=======
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
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
        launch { shooter?.set(PercentOutput(shooter.hardware.escConfig, 30.Percent)) }
        } finally {
            withContext(NonCancellable) {
            }
        }
    }

suspend fun Subsystems.turretTurnRight() = supervisorScope() {
    try {
        launch { turret?.spin(PercentOutput(turret.hardware.escConfig, 30.Percent)) }

    } finally {
        withContext(NonCancellable){

        }
    }
}
suspend fun Subsystems.turretTurnLeft() = supervisorScope() {
    try {
        launch { turret?.spin(PercentOutput(turret.hardware.escConfig, 30.Percent)) }
    } finally {
        withContext(NonCancellable){

        }
    }
}


>>>>>>> teleop2020
