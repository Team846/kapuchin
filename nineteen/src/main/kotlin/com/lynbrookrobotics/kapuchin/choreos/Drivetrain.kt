package com.lynbrookrobotics.kapuchin.choreos

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.control.math.kinematics.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.driver.*
import com.lynbrookrobotics.kapuchin.subsystems.drivetrain.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlinx.coroutines.isActive

suspend fun drivetrainTeleop(
        drivetrain: DrivetrainComponent,
        driver: DriverHardware,
        limelight: LimelightHardware
) = startChoreo("Drivetrain teleop") {

    val visionAlign by driver.visionAlign.readEagerly().withoutStamps

    choreography {
        whenever({ isActive }) {
            runWhile({ !visionAlign }) {
                drivetrain.teleop(driver)
            }
            runWhile({ visionAlign }) {
                limelightAlign(drivetrain, limelight)
            }
        }
    }
}

suspend fun optimizedLimelightTracking(
        drivetrain: DrivetrainComponent,
        limelight: LimelightHardware,
        tolerance: Angle = 10.Degree,
        drivetrainConversions: DrivetrainConversions
) = startChoreo("Optimized Limelight Tracking") {

    val distToNorm by limelight.distanceToNormal.readEagerly().withoutStamps
    val targetLocation by limelight.targetPosition.readEagerly().withoutStamps
    val distanceToTarget = targetLocation?.let {
        UomVector(it.x, it.y).abs
    }
    val startingTXValue by limelight.targetAngle.readEagerly().withoutStamps
    val acrossTx = distanceToTarget?.let { distance ->
        distToNorm?.let {normal ->
            startingTXValue?.let {tx ->
                sqrt(Dimensionless(((distance * distance) + (normal * normal) - (2 * normal * distance * cos(tx))).siValue)) * 1.Inch
            }

        }
    }
    val startingIsosAngle = distanceToTarget?.let { distance ->
        distToNorm?.let { normal ->
            acrossTx?.let { side ->
                acos(((distance * distance) + (side * side) - (normal * normal)) / (Dimensionless(2.0) * distance * normal))
            }

        }
    }
    val startingTurnAngle = startingTXValue?.let {
        startingIsosAngle?.let {angle ->
            angle - it
        }
    }
    val trackLength: Length = drivetrainConversions.trackLength //by drivetrainConversions.trackLength <- needs to be fixed but this is filler so the code doesn't crash
        choreography {
            if (startingTurnAngle != null) {
                drivetrain.turn(startingTurnAngle, tolerance / 2)
                drivetrain.limelightCurveDrive(limelight, trackLength , 0.5, drivetrainConversions)
            }
        }
    }



suspend fun limelightAlign(
        drivetrain: DrivetrainComponent,
        limelight: LimelightHardware,
        tolerance: Angle = 10.Degree
) = startChoreo("Limelight align") {
    val robotPosition by drivetrain.hardware.position.readEagerly().withoutStamps
    val targetPosition by limelight.targetPosition.readEagerly().withoutStamps

    val farEndPt = 3.Foot
    val closeEndPt = 2.Foot

    choreography {
        targetPosition?.let { visionSnapshot1 ->
            val robotSnapshot1 = robotPosition
            val mtrx = RotationMatrix(robotSnapshot1.bearing)
            val targetLoc = mtrx rz visionSnapshot1.vector

            if (visionSnapshot1.bearing in 0.Degree `±` tolerance) {
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

        drivetrain.limelightTracking(1.FootPerSecond, limelight)
    }
}
