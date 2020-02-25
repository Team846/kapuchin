package com.lynbrookrobotics.kapuchin.subsystems.limelight

import com.lynbrookrobotics.kapuchin.Field.ballDiameter
import com.lynbrookrobotics.kapuchin.Field.innerGoalDepth
import com.lynbrookrobotics.kapuchin.Field.targetDiameter
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.limelight.Pipeline.*
import com.lynbrookrobotics.kapuchin.subsystems.shooter.*
import com.lynbrookrobotics.kapuchin.subsystems.shooter.flywheel.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class LimelightConversions(val hardware: LimelightHardware) : Named by Named("Conversions", hardware) {
    private val skewTolerance by pref(1, Degree)
    private val innerGoalOffset by pref(29.25, Inch)
    private val targetHeight by pref(107, Inch)

    private val mountingIncline by pref(38, Degree)
    private val mounting by pref {
        val x by pref(0, Inch)
        val y by pref(0, Inch)
        val z by pref(24, Inch)
        ({ UomVector(x, y, z) })
    }

    val zoomOutSafetyZone by pref(40, Each)
    val zoomInSafetyZone by pref(10, Each)
    val zoomMultiplier by pref(2)

    val zoomInResolution by pref {
        val x by pref(240, Each)
        val y by pref(320, Each)
        ({ UomVector(x, y) })
    }
    val zoomInFov by pref {
        val x by pref(22.85, Degree)
        val y by pref(29.8, Degree)
        ({ UomVector(x, y) })
    }

    val zoomOutResolution by pref {
        val x by pref(720, Each)
        val y by pref(960, Each)
        ({ UomVector(x, y) })
    }
    val zoomOutFov by pref {
        val x by pref(45.7, Degree)
        val y by pref(59.6, Degree)
        ({ UomVector(x, y) })
    }

    private fun outerGoalPosition(sample: LimelightReading, skew: Angle) = with(sample) {

        val targetDistance = (targetHeight - mounting.z) / tan(mountingIncline + ty + when (pipeline) {
            ZoomInPanHigh -> zoomOutFov.y / 2
            ZoomInPanLow -> -zoomOutFov.y / 2
            else -> 0.Degree
        })

        val x = tan(tx) * targetDistance

        Position(x, targetDistance, skew)
    }

    fun goalPositions(sample: LimelightReading, skew: Angle): DetectedTarget {
        val outerGoal = outerGoalPosition(sample, skew)
        val offsetAngle = 90.Degree - skew

        val innerGoal = Position(
                innerGoalOffset * cos(offsetAngle) + outerGoal.x,
                innerGoalOffset * sin(offsetAngle) + outerGoal.y,
                skew
        )

        return DetectedTarget(innerGoal.takeUnless { skew > skewTolerance }, outerGoal)
    }



    /**
     * Calculate the horizontal and vertical offsets of the inner goal relative to the outer goal based on a "2D" view of the target.
     *
     * @author Sid R
     */
    fun innerGoalOffsets(target: DetectedTarget, shooterHeight: Length): Pair<Length, Length> {

        val horizontal = (innerGoalDepth * target.outer.x) / (target.outer.y + innerGoalDepth)
        val vertical = (innerGoalDepth * targetHeight) / (target.outer.y + innerGoalDepth)

        return horizontal to vertical
    }

    /**
     * Calculate the range of vertical angles a ball could enter the inner goal.
     *
     * @author Sid R
     */
    fun innerEntryAngleLimits(target: DetectedTarget, shooterHeight: Length, flywheel: FlywheelComponent): Pair<Angle, Angle> {
        val horizontalOffset = innerGoalOffsets(target, shooterHeight).first
        val verticalOffset = innerGoalOffsets(target, shooterHeight).second

        val hBot = sqrt(flywheel.hexagonCircleRadius.squared - horizontalOffset.squared) - verticalOffset
        val hTop = sqrt(flywheel.hexagonCircleRadius.squared - horizontalOffset.squared) + verticalOffset

        val downward = atan( (hTop + (2*verticalOffset) - (ballDiameter / 2 )) / innerGoalDepth)
        val upward = atan( (hBot - (ballDiameter / 2)) / (innerGoalDepth))
        return upward to downward
    }

    fun isWithinLimits(entryAngle: Angle, target: DetectedTarget, shooterHeight: Length, flywheel: FlywheelComponent): Boolean {
        if (entryAngle < 0.Degree) {
            if (entryAngle.abs <= innerEntryAngleLimits(target, shooterHeight, flywheel).second) return true
        }
        else {
            if (entryAngle.abs <= innerEntryAngleLimits(target, shooterHeight, flywheel).first) return true
        }
        return false
    }

}