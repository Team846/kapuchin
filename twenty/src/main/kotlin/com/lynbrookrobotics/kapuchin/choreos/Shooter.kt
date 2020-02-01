package com.lynbrookrobotics.kapuchin.choreos

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.subsystems.limelight.*
import com.lynbrookrobotics.kapuchin.subsystems.shooter.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlin.math.sqrt
import kotlin.math.atan

suspend fun Subsystems.aimAndShootPowerCell() = startChoreo("Shoot power cell") {

    fun requiredVelocities(
            flywheel: FlywheelComponent, hood: HoodComponent,
            hoodState: HoodState, targetHeight: Length
    ): Pair<Velocity, AngularVelocity> {
        val launchA = if (hoodState == HoodState.Down) hood.launchAngles.first else hood.launchAngles.second

        val ballVelocity = run {
            val dist = /*distanceToTarget(target)*/0.Foot // TODO
            val deltaHeight = targetHeight - flywheel.height

            val numerator = dist * dist * 1.EarthGravity
            val denominator = (dist * tan(launchA) - deltaHeight) * cos(launchA) * cos(launchA)

            sqrt(0.5) * Velocity(sqrt((numerator / denominator).siValue))
        }

        val flywheelOmega = with(flywheel) {
            (momentFactor * ballMass + ((2 * momentOfInertia) / (rollerRadius * rollerRadius))) * (ballVelocity * rollerRadius) / momentOfInertia
        } * Radian

        return ballVelocity to flywheelOmega
    }

    fun targetEntryAngle(hood: HoodComponent, hoodState: HoodState, ballVelocity: Velocity): Angle {
        val launchA = if (hoodState == HoodState.Down) hood.launchAngles.first else hood.launchAngles.second
        val dist = /*distanceToTarget(target)*/0.Foot // TODO
        val slope = ((-1 * dist * 1.EarthGravity) / (ballVelocity * ballVelocity * cos(launchA) * cos(launchA))) + tan(launchA)
        return atan(slope)
    }

    fun shotState(
            flywheel: FlywheelComponent, hood: HoodComponent,
            hoodState: HoodState, target: Position
    ): Pair<AngularVelocity, Angle> {
        val targetHeight = 0.Foot //TODO
        val (ballVelocity, flywheelOmega) = requiredVelocities(flywheel, hood, hoodState, targetHeight)
        val entryAngle = targetEntryAngle(hood, hoodState, ballVelocity)
        return flywheelOmega to entryAngle
    }

    choreography {
        // TODO get target, do nothing if null
        if (limelight == null) return@choreography
        val target = DetectedTarget(Position(0.Foot, 0.Foot, 0.Degree), Position(0.Foot, 0.Foot, 0.Degree))
        val targetHeight = 0.Foot

        if (flywheel != null && hood != null) {
            // TODO check if target is physically impossible to shoot to
            val downInner = target.inner
                    ?.let { shotState(flywheel, hood, HoodState.Down, it) }
                    ?.let { it.second in flywheel.innerGoalEntryTolerance && it.first < flywheel.maxOmega }
                    ?: false
            val downOuter = target.outer
                    ?.let { shotState(flywheel, hood, HoodState.Down, it) }
                    ?.let { it.second in flywheel.innerGoalEntryTolerance && it.first < flywheel.maxOmega }
                    ?: false
            val upInner = target.inner
                    ?.let { shotState(flywheel, hood, HoodState.Up, it) }
                    ?.let { it.second in flywheel.outerGoalEntryTolerance && it.first < flywheel.maxOmega }
                    ?: false
            val upOuter = target.outer
                    ?.let { shotState(flywheel, hood, HoodState.Up, it) }
                    ?.let { it.second in flywheel.outerGoalEntryTolerance && it.first < flywheel.maxOmega }
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