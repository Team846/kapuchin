package com.lynbrookrobotics.kapuchin.choreos

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.subsystems.limelight.*
import com.lynbrookrobotics.kapuchin.subsystems.shooter.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlin.math.sqrt

suspend fun Subsystems.aimAndShootPowerCell() = startChoreo("Shoot power cell") {

    // TODO unit tests for all methods

    fun requiredVelocities(
            flywheel: FlywheelComponent, hood: ShooterHoodComponent,
            hoodState: HoodState, target: DetectedTarget
    ): Pair<Pair<Velocity, Velocity>, Pair<AngularVelocity, AngularVelocity>> { // Returns a pair of pairs of the ball velocity and the required rpm to spin it to that velocity

        var outerPair = Pair(Velocity(0.0), AngularVelocity(0.0))
        var innerPair = Pair(Velocity(0.0), AngularVelocity(0.0))

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

            outerPair = ballVelocity to flywheelOmega

        }
        target.innerGoalPos?.let {
            val launchA = if (hoodState == HoodState.Down) hood.launchAngles.first else hood.launchAngles.second


            val dist = Length(sqrt((it.x * it.x + it.y * it.y).siValue))
            val deltaHeight = flywheel.targetHeight - flywheel.height

            val expression = ((dist * 1.EarthGravity) / (((dist * tan(launchA)) - deltaHeight) * cos(launchA) * cos(launchA))) * dist
            val ballVelocity = Velocity(sqrt(expression.siValue / 2))


            val flywheelOmega = with(flywheel) {
                AngularVelocity(((momentFactor * ballMass + (2 * momentOfInertia / (rollerRadius * rollerRadius))) * ballVelocity * rollerRadius / momentOfInertia).siValue)
                + slippage
            }

            innerPair =  ballVelocity to flywheelOmega
        }
        return (outerPair.first to innerPair.first) to (outerPair.second to innerPair.second)
    }

    fun targetEntryAngle(hood: ShooterHoodComponent, hoodState: HoodState, ballVelocity: Velocity, target: DetectedTarget): Angle { // Returns the entry angle of the ball at outer goal
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
            ): Pair< Pair<AngularVelocity, Angle>, Pair<AngularVelocity, Angle> > { // Returns a pair of the pair of required ang. vel and the entry angle of the ball
        val (ballVelocity, flywheelOmega) = requiredVelocities(flywheel, hood, hoodState, target)

        val entryAngleOuter = targetEntryAngle(hood, hoodState, ballVelocity.first, target)
        val entryAngleInner = targetEntryAngle(hood, hoodState, ballVelocity.second, target)

        return (flywheelOmega.first to entryAngleOuter) to (flywheelOmega.second to entryAngleInner)
    }



    fun offsets(flywheel: FlywheelComponent, target: DetectedTarget): Pair<Length, Length> { // Horizontal and Vertical offsets of inner goal with respect to outer goal

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

    fun entryAngleLimits(flywheel: FlywheelComponent, target: DetectedTarget): Pair<Angle, Angle> { // Entry angle tolerance for inner goal

        val downward = atan(((flywheel.hexagonHeight / 2) + offsets(flywheel, target).second) / flywheel.outerInnerDiff) // angle of approach from below
        val upward = 90.Degree - atan(flywheel.outerInnerDiff / ((flywheel.hexagonHeight / 2) - offsets(flywheel, target).second)) // angle of approach from upward
        return downward to upward

    }

    fun innerGoalPossible(flywheel: FlywheelComponent, target: DetectedTarget): Boolean {
        val horizontal  = offsets(flywheel, target).first // horizontal offset of inner goal with respect to outer goal
        val vertical = offsets(flywheel, target).second // vertical offset of inner goal with respect to outer goal
        return ((horizontal * horizontal) + (vertical * vertical)) < flywheel.boundingCircleRadius * flywheel.boundingCircleRadius
    }

    fun getIdealShot(): ShooterState? { // THIS is the function to get shooter-state data from
        if (limelight == null) return null

        val reading by limelight.hardware.readings.readEagerly().withoutStamps // Get a limelight reading


        reading?.let {
            val skew = it.tx
            val limelight = LimelightComponent(limelight.hardware)
            val target = limelight.innerGoalPos(it, skew) // Get the position of inner goal


            if (flywheel != null && hood != null) {

                val downInner = target.innerGoalPos // Checks if the hood-down and inner goal state is impossible
                        ?.let { shotState(flywheel, hood, HoodState.Down, target) }
                        ?.let {
                            it.second.first < flywheel.maxOmega
                                    && it.second.second in entryAngleLimits(flywheel, target).first..entryAngleLimits(flywheel, target).second
                                    && innerGoalPossible(flywheel, target)
                        }
                        ?: false
                val downOuter = target.outerGoalPos // Checks if the hood-down and outer goal state is impossible
                        ?.let { shotState(flywheel, hood, HoodState.Down, target) }
                        ?.let { it.first.second.abs < flywheel.outerGoalEntryTolerance && it.first.first < flywheel.maxOmega }
                        ?: false
                val upInner = target.innerGoalPos // Checks if the hood-up and inner goal state is impossible
                        ?.let { shotState(flywheel, hood, HoodState.Up, target) }
                        ?.let {
                            it.second.first < flywheel.maxOmega
                                    && it.second.second in entryAngleLimits(flywheel, target).first..entryAngleLimits(flywheel, target).second
                                    && innerGoalPossible(flywheel, target)
                        }
                        ?: false
                val upOuter = target.outerGoalPos // Checks if the hood-up and outer goal state is impossible
                        ?.let { shotState(flywheel, hood, HoodState.Up, target) }
                        ?.let { it.first.second.abs < flywheel.outerGoalEntryTolerance && it.first.first < flywheel.maxOmega }
                        ?: false

                if (downInner && downOuter && upInner && upOuter) return null // Returns null if all states are impossible

                // This is where the function returns a data class of shooter rpm, hood state, and angle to turn
                if (!downInner) return ShooterState(shotState(flywheel, hood, HoodState.Down, target).second.first, HoodState.Down, target.innerGoalPos!!.bearing)
                else if (!upInner) return ShooterState(shotState(flywheel, hood, HoodState.Up, target).second.first, HoodState.Up, target.innerGoalPos!!.bearing)
                else if (!downOuter) return ShooterState(shotState(flywheel, hood, HoodState.Down, target).first.first, HoodState.Down, target.outerGoalPos!!.bearing)
                else return ShooterState(shotState(flywheel, hood, HoodState.Up, target).first.first, HoodState.Up, target.outerGoalPos!!.bearing)

            }
        }
        return null
    }


    choreography {



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


suspend fun Subsystems.shooterTeleop() = startChoreo("Shooter Teleop") {
    choreography {

    }
}
