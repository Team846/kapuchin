package com.lynbrookrobotics.kapuchin.subsystems.limelight

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.subsystems.limelight.Pipeline.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

data class LimelightReading(
        val tx: Angle, val ty: Angle,
        val tx0: Dimensionless, val ty0: Dimensionless,
        val thor: Pixel, val tvert: Pixel,
        val ta: Dimensionless, val pipeline: Pipeline?
)

enum class Pipeline(val number: Int) {
    ZoomOut(0), ZoomInPanHigh(1), ZoomInPanMid(2), ZoomInPanLow(3), DriverStream(4)
}

data class DetectedTarget(val inner: Position?, val outer: Position?)

fun outerGoalPosition(sample: LimelightReading) = with(sample) {
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

fun goalPositions(sample: LimelightReading, skew: Angle = sample.tx): DetectedTarget {
    val outerGoal = outerGoalPosition(sample)
    val offsetAngle = 90.Degree - skew

    val innerGoal = Position(
            innerGoalOffset * cos(offsetAngle) + outerGoal.x,
            innerGoalOffset * sin(offsetAngle) + outerGoal.y,
            skew
    )

    return DetectedTarget(innerGoal.takeUnless { skew > skewTolerance }, outerGoal)
}