package com.lynbrookrobotics.kapuchin.choreos

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.kinematics.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.drivetrain.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

suspend fun LimelightHardware.perpendicularAlign(
        drivetrain: DrivetrainComponent,
        tolerance: Angle = 10.Degree
) = startChoreo("Perpendicular align") {
    val robotPosition by drivetrain.hardware.position.readEagerly().withoutStamps
    val targetPosition by targetPosition.readEagerly().withoutStamps

    val farEndPt = 3.Foot
    val closeEndPt = 2.Foot

    choreography {
        targetPosition?.let { visionSnapshot1 ->
            val robotSnapshot1 = robotPosition
            val mtrx = RotationMatrix(robotSnapshot1.bearing)
            val targetLoc = mtrx rz visionSnapshot1.vector

            if (visionSnapshot1.bearing.abs < tolerance) {
                val perpPt = mtrx rz UomVector(
                        closeEndPt * sin(0.Degree),
                        closeEndPt * cos(0.Degree)
                )

                val waypt = robotSnapshot1.vector + targetLoc - perpPt

                drivetrain.waypoint(
                        trapezoidalMotionProfile(
                                0.5.FootPerSecondSquared,
                                3.FootPerSecond
                        ), waypt, 4.Inch
                )
            } else {
                val farPerpPt = mtrx rz UomVector(
                        farEndPt * sin(visionSnapshot1.bearing),
                        farEndPt * cos(visionSnapshot1.bearing)
                )

                val waypt = robotSnapshot1.vector + targetLoc - farPerpPt

                drivetrain.waypoint(
                        trapezoidalMotionProfile(
                                0.5.FootPerSecondSquared,
                                3.FootPerSecond
                        ), waypt, 4.Inch
                )

                drivetrain.turn(
                        robotSnapshot1.bearing + visionSnapshot1.bearing,
                        tolerance / 2
                )
            }
        }

        drivetrain.visionSnapshotTracking(1.FootPerSecond, this@perpendicularAlign)
    }
}

suspend fun DrivetrainComponent.visionSnapshotTracking(speed: Velocity, limelight: LimelightHardware) = startRoutine("Vision snapshot tracking") {
    val targetAngle by limelight.targetAngle.readOnTick.withoutStamps
    val robotPosition by hardware.position.readOnTick.withoutStamps
    val uni = UnicycleDrive(this@visionSnapshotTracking, this@startRoutine)

    val target = targetAngle?.let { it + robotPosition.bearing }

    controller {
        if (target != null) {
            val (targs, _) = uni.speedAngleTarget(speed, target)

            val nativeL = hardware.conversions.nativeConversion.native(targs.left)
            val nativeR = hardware.conversions.nativeConversion.native(targs.right)

            TwoSided(
                    VelocityOutput(velocityGains, nativeL),
                    VelocityOutput(velocityGains, nativeR)
            )
        } else null
    }
}

suspend fun DrivetrainComponent.visionActiveTracking(speed: Velocity, limelight: LimelightHardware) = startRoutine("Vision snapshot tracking") {
    val targetAngle by limelight.targetAngle.readOnTick.withoutStamps
    val robotPosition by hardware.position.readOnTick.withoutStamps
    val uni = UnicycleDrive(this@visionActiveTracking, this@startRoutine)

    controller {
        targetAngle?.let { targetSnapshot ->
            val (targs, _) = uni.speedAngleTarget(speed, targetSnapshot + robotPosition.bearing)

            val nativeL = hardware.conversions.nativeConversion.native(targs.left)
            val nativeR = hardware.conversions.nativeConversion.native(targs.right)

            TwoSided(
                    VelocityOutput(velocityGains, nativeL),
                    VelocityOutput(velocityGains, nativeR)
            )
        }
    }
}

suspend fun DrivetrainComponent.lineActiveTracking(speed: Velocity, lineScanner: LineScannerHardware) = startRoutine("Point with line scanner") {
    val linePosition by lineScanner.linePosition.readOnTick.withoutStamps
    val uni = UnicycleDrive(this@lineActiveTracking, this@startRoutine)

    controller {
        val errorA = linePosition?.let {
            -atan(it / lineScanner.lookAhead)
        } ?: 0.Degree

        val (targetL, targetR) = uni.speedTargetAngleError(speed, errorA)

        val nativeL = hardware.conversions.nativeConversion.native(targetL)
        val nativeR = hardware.conversions.nativeConversion.native(targetR)

        TwoSided(
                VelocityOutput(velocityGains, nativeL),
                VelocityOutput(velocityGains, nativeR)
        )
    }
}