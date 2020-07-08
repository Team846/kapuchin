package com.lynbrookrobotics.kapuchin.subsystems.limelight

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.limelight.Pipeline.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class LimelightConversions(val hardware: LimelightHardware) : Named by Named("Conversions", hardware) {
    private val skewTolerance by pref(1, Degree)
    private val innerGoalOffset by pref(29.25, Inch)
    private val targetHeight by pref(107, Inch)

    private val mountingIncline by pref(38, Degree)
    val mountingBearing by pref(-0.35, Degree)
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

        val x = tan(tx + mountingBearing) * targetDistance

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

}