package com.lynbrookrobotics.kapuchin.subsystems.shooter

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.Field.ballDiameter
import com.lynbrookrobotics.kapuchin.Field.ballMass
import com.lynbrookrobotics.kapuchin.Field.targetDiameter
import com.lynbrookrobotics.kapuchin.Field.targetHeight
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.subsystems.limelight.*
import com.lynbrookrobotics.kapuchin.subsystems.shooter.Goal.*
import com.lynbrookrobotics.kapuchin.subsystems.shooter.ShooterHoodState.*
import com.lynbrookrobotics.kapuchin.subsystems.shooter.flywheel.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlin.math.sqrt

// UOM extension functions to make shooter math cleaner
val Length.squared: `L²` get() = this * this
val Dimensionless.squared: Dimensionless get() = this * this
val Velocity.squared: `L²⋅T⁻²` get() = this * this
fun sqrt(a: `L²⋅T⁻²`): Velocity = Velocity(sqrt(a.siValue))
fun sqrt(a: `L²`): Length = Length(sqrt(a.siValue))

enum class Goal { Inner, Outer }
data class ShotState(val flywheel: AngularVelocity, val hood: ShooterHoodState, val goal: Goal, val entryAngle: Angle)

/**
 * Calculate the best shot state given the current target.
 * Chooses between the 4 combinations of shooter hood up/down and inner/outer goal.
 *
 * @author Andy
 *
 * @return the best shot state, null if shot is impossible.
 */
fun Subsystems.bestShot(target: DetectedTarget): ShotState? {
    if (limelight == null || flywheel == null || shooterHood == null) return null

    val innerLimits = limelight.hardware.conversions.innerEntryAngleLimits(target, flywheel.shooterHeight)
    val outerLimits = `±`(90.Degree - atan2(ballDiameter, targetDiameter / 2))
    val boundingCircleRadius = (targetDiameter / 2) - (ballDiameter / 2) // TODO maybe use slightly larger bounding circle radius? sid r
    val innerGoalPossible = limelight.hardware.conversions.innerGoalOffsets(target, flywheel.shooterHeight)
            .let { (hor, vert) -> (hor.squared + vert.squared) < boundingCircleRadius.squared }

    val innerUp = target.inner?.let { calculateShot(it, Up, Inner, innerLimits, flywheel, shooterHood).takeIf { innerGoalPossible } }
    val innerDown = target.inner?.let { calculateShot(it, Down, Inner, innerLimits, flywheel, shooterHood).takeIf { innerGoalPossible } }
    val outerUp = calculateShot(target.outer, Up, Outer, outerLimits, flywheel, shooterHood)
    val outerDown = calculateShot(target.outer, Down, Outer, outerLimits, flywheel, shooterHood)

    // If both hood states are possible, choose the one with the better entry angle
    // Otherwise, choose the first target that works prioritizing inner goal
    return if (innerUp != null && innerDown != null) if (innerUp.entryAngle.abs < innerDown.entryAngle.abs) innerUp else innerDown
    else if (outerUp != null && outerDown != null) if (outerUp.entryAngle.abs < outerDown.entryAngle.abs) outerUp else outerDown
    else listOfNotNull(innerUp, innerDown, outerUp, outerDown).firstOrNull()
}

/**
 * Calculate the required flywheel velocity and the entry angle of a shot given the target's position and launch angle.
 *
 * @author Sam (math)
 * @author Andy (code)
 *
 * @return a pair of flywheel velocity to entry angle.
 */
private fun calculateShot(
        target: Position,
        shooterHoodState: ShooterHoodState,
        goal: Goal,
        entryAngleLimits: ClosedRange<Angle>,
        flywheel: FlywheelComponent,
        shooterHood: ShooterHoodComponent
): ShotState? {
    val launchAngle = shooterHoodState.launchAngle(shooterHood)
    val distToBase = sqrt(target.x.squared + target.y.squared)
    val height = targetHeight - flywheel.shooterHeight

    val ballVelocity = sqrt(0.5) * sqrt((distToBase.squared * 1.EarthGravity) / ((distToBase * tan(launchAngle) - height) * cos(launchAngle).squared))

    val flywheelVelocity = with(flywheel) {
        ((momentFactor * ballMass + (2 * momentOfInertia / rollerRadius.squared)) * ballVelocity * rollerRadius) / momentOfInertia * fudgeFactor
    } * Radian

    val shotEntrySlope = -((distToBase * 1.EarthGravity) / (ballVelocity.squared * cos(launchAngle).squared)) + tan(launchAngle)
    val shotEntryAngle = atan(shotEntrySlope)

    return ShotState(flywheelVelocity, shooterHoodState, goal, shotEntryAngle)
            .takeIf { shotEntryAngle in entryAngleLimits }
            .takeIf { flywheelVelocity <= flywheel.maxSpeed }
}
