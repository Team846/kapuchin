package com.lynbrookrobotics.kapuchin.subsystems.shooter

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.subsystems.shooter.ShooterHoodState.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

/**
 * Performs a *quadratic* regression on the target incline to generate an RPM
 * y = ax² + bx + c
 *
 * @author Sid K
 *
 * @return an RPM
 */
private fun inclineToRPM(incline: Angle): AngularVelocity {
    val a: Double = 1.0 //placeholder value for regression
    val b: Double = 1.0 // "
    val c: Double = 1.0 // "

    return (a * incline.Degree * incline + b * incline + c.Degree).Degree.Rpm
}

private fun calculateShot(
        target: Position,
        shooterHoodState: ShooterHoodState,
        goal: Goal,
        entryAngleLimits: ClosedRange<Angle>,

        launchAngle: Angle,

        maxSpeed: AngularVelocity,
        shooterHeight: Length,
        hoodUpLaunch: Angle,
        hoodDownLaunch: Angle
): ShotState? {
    val distToBase = sqrt(target.x.squared + target.y.squared)
    val height = Field.targetHeight - shooterHeight
    val incline = atan(height / distToBase)
    val hoodAngle = when (shooterHoodState) {
        Up -> {
            hoodUpLaunch
        }
        Down -> {
            hoodDownLaunch
        }
    }

    if (incline > hoodAngle) {
        return null
    }
    val ballVelocity = kotlin.math.sqrt(0.5) * sqrt((distToBase.squared * 1.EarthGravity) / ((distToBase * tan(launchAngle) - height) * cos(launchAngle).squared))

    val flywheelVelocity = inclineToRPM(incline)

    val shotEntrySlope = -((distToBase * 1.EarthGravity) / (ballVelocity.squared * cos(launchAngle).squared)) + tan(launchAngle)
    val shotEntryAngle = atan(shotEntrySlope)

    return ShotState(flywheelVelocity, shooterHoodState, goal, shotEntryAngle)
            .takeIf { shotEntryAngle in entryAngleLimits }
            .takeIf { flywheelVelocity <= maxSpeed }
}