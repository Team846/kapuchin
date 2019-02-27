package com.lynbrookrobotics.kapuchin.choreos

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.control.math.kinematics.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.logging.*
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
