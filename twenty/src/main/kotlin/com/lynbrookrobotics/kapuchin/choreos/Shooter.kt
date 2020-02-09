package com.lynbrookrobotics.kapuchin.choreos

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.subsystems.limelight.*
import com.lynbrookrobotics.kapuchin.subsystems.shooter.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlin.math.sqrt

import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext

suspend fun Subsystems.aimAndShootPowerCell() = startChoreo("Shoot power cell") {

    fun requiredVelocities(
            flywheel: FlywheelComponent, hood: ShooterHoodComponent,
            hoodState: HoodState, target: DetectedTarget
    ): Pair<Velocity, AngularVelocity> {

        target.outerGoalPos?.let {
            val launchA = if (hoodState == HoodState.Down) hood.launchAngles.first else hood.launchAngles.second


            val dist = Length(sqrt((it.x * it.x + it.y * it.y).siValue))
            val deltaHeight = flywheel.targetHeight - flywheel.height

            val expression = ((dist * 1.EarthGravity) / (((dist * tan(launchA)) - deltaHeight) * cos(launchA) * cos(launchA))) * dist
            val ballVelocity = Velocity(sqrt(expression.siValue / 2))


            val flywheelOmega = with(flywheel) {
                AngularVelocity(((momentFactor * ballMass + (2 * momentOfInertia / (rollerRadius * rollerRadius))) * ballVelocity * rollerRadius / momentOfInertia).siValue)
                + slippage
            }

            return ballVelocity to flywheelOmega
        }
        return Velocity(0.0) to AngularVelocity(0.0)
    }

    fun targetEntryAngle(hood: ShooterHoodComponent, hoodState: HoodState, ballVelocity: Velocity, target: DetectedTarget): Angle {
        target.outerGoalPos?.let {
            val launchA = if (hoodState == HoodState.Down) hood.launchAngles.first else hood.launchAngles.second
            val dist = Length(sqrt((it.x * it.x + it.y * it.y).siValue))
            val slope = ((-1 * dist * 1.EarthGravity) / (ballVelocity * ballVelocity * cos(launchA) * cos(launchA))) + tan(launchA)
            return atan(slope).abs
        }
        return 0.Degree
    }

    fun shotState(
            flywheel: FlywheelComponent, hood: ShooterHoodComponent,
            hoodState: HoodState, target: DetectedTarget
            ): Pair<AngularVelocity, Angle> {
        val (ballVelocity, flywheelOmega) = requiredVelocities(flywheel, hood, hoodState, target)
        val entryAngle = targetEntryAngle(hood, hoodState, ballVelocity, target)
        return flywheelOmega to entryAngle
    }



    fun offsets(flywheel: FlywheelComponent, target: DetectedTarget): Pair<Length, Length> { // Inner goal offsets

        target.outerGoalPos?.let {
            val dist = Length(sqrt((it.x * it.x + (it.y * it.y)).siValue))
            val horizontal = flywheel.outerInnerDiff * tan(it.bearing)
            val vertical = with(flywheel)   {
                (outerInnerDiff * (targetHeight - height)) / (dist * cos(it.bearing))
            }
            return horizontal to vertical
        }
        return Length(0.0) to Length(0.0)

    }

    fun entryAngleLimits(flywheel: FlywheelComponent, target: DetectedTarget): Pair<Angle, Angle> { // Entry angle tolerance for inner outer goal

        val downward = atan(((flywheel.hexagonHeight / 2) + offsets(flywheel, target).second) / flywheel.outerInnerDiff) // approach from below
        val upward = 90.Degree - atan(flywheel.outerInnerDiff / ((flywheel.hexagonHeight / 2) - offsets(flywheel, target).second)) // approach from upward
        return downward to upward

    }

    fun innerGoalPossible(flywheel: FlywheelComponent, target: DetectedTarget): Boolean {
        val horizontal  = offsets(flywheel, target).first
        val vertical = offsets(flywheel, target).second
        return ((horizontal * horizontal) + (vertical * vertical)) < flywheel.boundingCircleRadius * flywheel.boundingCircleRadius
    }


    choreography {
        // TODO get target, do nothing if null
        if (limelight == null) return@choreography
        val reading by limelight.hardware.readings.readEagerly().withoutStamps


        reading?.let {
            val skew = it.tx
            val limelight = LimelightComponent(limelight.hardware)
            val target = limelight.innerGoalPos(it, skew)


            if (flywheel != null && hood != null) {
                // TODO check if target is physically impossible to shoot to
                val downInner = target.innerGoalPos
                        ?.let { shotState(flywheel, hood, HoodState.Down, target) }
                        ?.let {
                            it.first < flywheel.maxOmega
                                    && it.second in entryAngleLimits(flywheel, target).first..entryAngleLimits(flywheel, target).second
                                    && innerGoalPossible(flywheel, target)
                        }
                        ?: false
                val downOuter = target.outerGoalPos
                        ?.let { shotState(flywheel, hood, HoodState.Down, target) }
                        ?.let { it.second.abs < flywheel.outerGoalEntryTolerance && it.first < flywheel.maxOmega }
                        ?: false
                val upInner = target.innerGoalPos
                        ?.let { shotState(flywheel, hood, HoodState.Up, target) }
                        ?.let {
                            it.first < flywheel.maxOmega
                                    && it.second in entryAngleLimits(flywheel, target).first..entryAngleLimits(flywheel, target).second
                                    && innerGoalPossible(flywheel, target)
                        }
                        ?: false
                val upOuter = target.outerGoalPos
                        ?.let { shotState(flywheel, hood, HoodState.Up, target) }
                        ?.let { it.second.abs < flywheel.outerGoalEntryTolerance && it.first < flywheel.maxOmega }
                        ?: false

                if (downInner && downOuter && upInner && upOuter) return@choreography // Exits choreo if all states are impossible

                if (!downInner) ShooterState(shotState(flywheel, hood, HoodState.Down, target).first, HoodState.Down)
                else if (!upInner) ShooterState(shotState(flywheel, hood, HoodState.Up, target).first, HoodState.Up)
                else if (!downOuter) ShooterState(shotState(flywheel, hood, HoodState.Down, target).first, HoodState.Down)
                else ShooterState(shotState(flywheel, hood, HoodState.Up, target).first, HoodState.Up)

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
         * Check which state works
         * Launch hood state set
         * Wait until flywheel and turret are set
         * Spin feeder roller
         *
         * If shooting multiple
         */
        }

    }

}

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
        launch { feederRoller?.spin(30.Rpm) }
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
        withContext(NonCancellable) {

        }
    }
}

suspend fun Subsystems.turretTurnLeft() = supervisorScope() {
    try {
        launch { turret?.spin(PercentOutput(turret.hardware.escConfig, 30.Percent)) }
    } finally {
        withContext(NonCancellable) {

        }
    }
}
