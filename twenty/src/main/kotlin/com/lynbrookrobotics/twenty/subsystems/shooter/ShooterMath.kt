package com.lynbrookrobotics.twenty.subsystems.shooter

import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.twenty.Subsystems
import com.lynbrookrobotics.twenty.subsystems.limelight.LimelightReading
import com.lynbrookrobotics.twenty.subsystems.limelight.Pipeline
import com.lynbrookrobotics.twenty.subsystems.shooter.flywheel.FlywheelComponent
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*


fun Subsystems.distanceToGoal(sample: LimelightReading, pitch: Angle): Length = with(limelight.hardware.conversions) {
    val mountingAngle = (if (hardware.invertTx) -mountingIncline + pitch else mountingIncline - pitch)
    val angle = mountingAngle + sample.ty + when (sample.pipeline) {
        Pipeline.ZoomInPanHigh -> zoomInFov.y / 2
        Pipeline.ZoomInPanLow -> -zoomInFov.y / 2
        else -> 0.Degree
    }

    val targetDistance = -(targetHeight - mounting.z) / tan(angle)
    log(Debug) { "Goal position: ${targetDistance.Foot} Feet" }
    targetDistance
}

fun Subsystems.targetFlywheelSpeed(flywheel: FlywheelComponent, snapshot: LimelightReading): AngularVelocity {
    var distance = distanceToGoal(snapshot, 0.Degree)

    if (distance > flywheel.innerPortDistanceThreshold) {
        log(Debug) { "Distance: ${distance.Foot} ft, aiming for outer" }
        val delta = 0.Foot // TODO sid
        distance -= delta

        // TODO sid

        // if skew > tolerance, aim for outer
    }

    return flywheel.hardware.conversions.rpmCurve(distance)
}