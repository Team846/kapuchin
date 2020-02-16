package com.lynbrookrobotics.kapuchin.subsystems.shooter

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.subsystems.limelight.*
import com.lynbrookrobotics.kapuchin.subsystems.shooter.ShooterHoodState.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

// UOM extension functions to make shooter math cleaner
private val Length.squared: `L²` get() = this * this
private val Dimensionless.squared: Dimensionless get() = this * this
private val Velocity.squared: `L²⋅T⁻²` get() = this * this
private fun sqrt(a: `L²⋅T⁻²`): Velocity = Velocity(kotlin.math.sqrt(a.siValue))
private fun sqrt(a: `L²`): Length = Length(kotlin.math.sqrt(a.siValue))

data class ShotState(val flywheelVelocity: AngularVelocity, val entryAngle: Angle, val shooterHoodState: ShooterHoodState)

/**
 * Calculate the best shot state given the current target.
 * Chooses between the 4 combinations of shooter hood up/down and inner/outer goal.
 *
 * @author Andy
 *
 * @return the best shot state, null if shot is impossible.
 */
fun Subsystems.bestShot(target: DetectedTarget): ShotState? {
    if (flywheel == null || shooterHood == null) return null

    val innerLimits = innerEntryAngleLimits(target, flywheel)
    val outerLimits = -flywheel.outerEntryAngleLimit..flywheel.outerEntryAngleLimit
    val innerGoalPossible = target.outer
            ?.let { innerGoalOffsets(it, flywheel) }
            ?.let { (hor, vert) -> (hor.squared + vert.squared) < flywheel.boundingCircleRadius.squared }
            ?: false

    return listOfNotNull(
            target.inner?.let { calculateShot(it, Up, innerLimits, flywheel, shooterHood).takeIf { innerGoalPossible } },
            target.inner?.let { calculateShot(it, Down, innerLimits, flywheel, shooterHood).takeIf { innerGoalPossible } },
            target.outer?.let { calculateShot(it, Up, outerLimits, flywheel, shooterHood) },
            target.outer?.let { calculateShot(it, Down, outerLimits, flywheel, shooterHood) }
    ).firstOrNull()
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
        target: Position?,
        shooterHoodState: ShooterHoodState,
        entryAngleLimits: ClosedRange<Angle>,
        flywheel: FlywheelComponent,
        shooterHood: ShooterHoodComponent
): ShotState? {
    if (target == null) return null

    val launchAngle = shooterHood.launchAngles(shooterHoodState)
    val distToBase = sqrt(target.x.squared + target.y.squared)
    val height = flywheel.targetHeight - flywheel.shooterHeight

    val ballVelocity = kotlin.math.sqrt(0.5) * sqrt((distToBase.squared * 1.EarthGravity) / ((distToBase * tan(launchAngle) - height) * cos(launchAngle).squared))

    val flywheelVelocity = with(flywheel) {
        ((momentFactor * ballMass + (2 * momentOfInertia / rollerRadius.squared)) * ballVelocity * rollerRadius) / momentOfInertia * fudgeFactor
    } * Radian

    val shotEntrySlope = -((distToBase * 1.EarthGravity) / (ballVelocity.squared * cos(launchAngle).squared)) + tan(launchAngle)
    val shotEntryAngle = atan(shotEntrySlope)

    return ShotState(flywheelVelocity, shotEntryAngle, shooterHoodState)
            .takeIf { shotEntryAngle in entryAngleLimits }
}

/**
 * Calculate the horizontal and vertical offsets of the inner goal relative to the outer goal based on a "2D" view of the target.
 *
 * @author Sid R
 */
private fun innerGoalOffsets(outer: Position, flywheel: FlywheelComponent): Pair<Length, Length> {
    val distToBase = sqrt(outer.x.squared + outer.y.squared)

    val horizontal = flywheel.innerGoalDepth * tan(outer.bearing)
    val vertical = with(flywheel) { (innerGoalDepth * (targetHeight - shooterHeight)) / (distToBase * cos(outer.bearing)) }

    return horizontal to vertical
}

/**
 * Calculate the range of vertical angles a ball could enter the inner goal.
 *
 * @author Sid R
 */
private fun innerEntryAngleLimits(target: DetectedTarget, flywheel: FlywheelComponent): ClosedRange<Angle> = target.outer?.let { outer ->
    val horizontalOffset = innerGoalOffsets(outer, flywheel).first

    val downward = atan(((flywheel.targetDiameter / 2) + horizontalOffset) / flywheel.innerGoalDepth)
    val upward = 90.Degree - atan(flywheel.innerGoalDepth / ((flywheel.targetDiameter / 2) - horizontalOffset))
    return downward..upward
} ?: 0.Degree..0.Degree
