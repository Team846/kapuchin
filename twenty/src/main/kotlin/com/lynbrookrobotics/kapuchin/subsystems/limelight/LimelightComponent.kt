package com.lynbrookrobotics.kapuchin.subsystems.limelight

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.limelight.Pipeline.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class LimelightComponent(hardware: LimelightHardware) : Component<LimelightComponent, LimelightHardware, Pipeline?>(hardware) {

    private fun targetPosition(sample: LimelightReading) = with(sample) {
        val aspect = thor / tvert
        val skew = acos(aspect / aspect0 minMag 1.Each)

        val targetDistance = when (pipeline) {
            ZoomInPanHigh -> (targetHeight - mounting.z) / tan(mountingIncline + ty + zoomOutFov.y / 2)
            ZoomInPanLow -> (targetHeight - mounting.z) / tan(mountingIncline + ty - zoomOutFov.y / 2)
            else -> (targetHeight - mounting.z) / tan(mountingIncline + ty)
        }

        val x = tan(tx) * targetDistance

        Position(x, targetDistance, skew)
    }

    fun innerGoalPos(sample: LimelightReading, skew: Angle): DetectedTarget {
        val outerGoalPos = targetPosition(sample)
        val offsetAngle = 90.Degree - skew

        val innerGoal = DetectedTarget(Position(
                innerGoalOffset * cos(offsetAngle) + outerGoalPos.x,
                innerGoalOffset * sin(offsetAngle) + outerGoalPos.y,
                skew
        ), outerGoalPos)

        return if (skew > skewTolerance) {
            DetectedTarget(null, targetPosition(sample))
        } else {
            innerGoal
        }

    }

    private val skewTolerance by pref(1, Degree)
    private val innerGoalOffset by pref(29.25, Inch)
    private val targetHeight by pref(107, Inch)
    private val aspect0 by pref {
        val thor by pref(226)
        val tvert by pref(94)
        ({ thor / tvert.toDouble() })
    }

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
        val x by pref(20.5, Degree)
        val y by pref(28, Degree)
        ({ UomVector(x, y) })
    }

    val zoomOutResolution by pref {
        val x by pref(720, Each)
        val y by pref(960, Each)
        ({ UomVector(x, y) })
    }
    val zoomOutFov by pref {
        val x by pref(41, Degree)
        val y by pref(56, Degree)
        ({ UomVector(x, y) })
    }

    override val fallbackController: LimelightComponent.(Time) -> Pipeline? = { Pipeline.ZoomOut }

    override fun LimelightHardware.output(value: Pipeline?) {
        pipelineEntry.setNumber(value?.number)
    }
}