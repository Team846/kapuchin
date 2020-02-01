package com.lynbrookrobotics.kapuchin.subsystems.limelight

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.limelight.DetectedTarget.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class LimelightComponent(hardware: LimelightHardware) : Component<LimelightComponent, LimelightHardware, LimelightOutput>(hardware, EventLoop) {

    private fun targetPosition(sample: LimelightReading) = sample.run {
        val aspect = thor / tvert

        val skew = acos(aspect / aspect0 minMag 1.Each)
        val distance = (targetHeight - mounting.z) / tan(mountingIncline + ty)
        val x = tan(tx) * distance

        Position(x, distance, skew)
    }
    private fun innerGoalPos(sample: LimelightReading, skew: Angle?)
    {
        val outerGoalPos = targetPosition(sample)
        if(skew == null)
        {
            val tSkew = targetPosition(sample).bearing
            val offsetAngle = 90.Degree - tSkew

            val innerGoal = InnerGoal(Position(
                    innerGoalOffset * cos(offsetAngle) + outerGoalPos.x,
                    innerGoalOffset * sin(offsetAngle) + outerGoalPos.y,
                    tSkew
            ))
            if (tSkew > skewTolerance)
            {
                OuterGoal(targetPosition(sample))
            }
            else{
                innerGoal
            }
        }
        else {
            val offsetAngle = 90.Degree - skew

            val innerGoal = InnerGoal(Position(
                    innerGoalOffset * cos(offsetAngle) + outerGoalPos.x,
                    innerGoalOffset * sin(offsetAngle) + outerGoalPos.y,
                    skew
            ))
            if (skew > skewTolerance)
            {
                OuterGoal(targetPosition(sample))
            }
            else{
                innerGoal
            }
        }

    }

    private val skewTolerance by pref(1,Degree)
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

    val zoomOutSafetyZone by pref(40, Pixel)
    val zoomInSafetyZone by pref(10, Pixel)
    val zoomMultiplier by pref(2)

    val zoomInResolution by pref {
        val x by pref(320, Pixel)
        val y by pref(240, Pixel)
        ({ UomVector(x, y) })
    }
    val zoomInFov by pref {
        val x by pref(28, Degree)
        val y by pref(20.5, Degree)
        ({ UomVector(x, y) })
    }

    val zoomOutResolution by pref {
        val x by pref(960, Pixel)
        val y by pref(720, Pixel)
        ({ UomVector(x, y) })
    }
    val zoomOutFov by pref {
        val x by pref(56, Degree)
        val y by pref(41, Degree)
        ({ UomVector(x, y) })
    }

    override val fallbackController: LimelightComponent.(Time) -> LimelightOutput = { LimelightOutput(Pipeline.ZoomOut,0,0) }

    override fun LimelightHardware.output(value: LimelightOutput) {
        pipelineEntry.setNumber(value.pipe?.number)
        panEntryX.setNumber(value.panX)
        panEntryY.setNumber(value.panY)
    }
}