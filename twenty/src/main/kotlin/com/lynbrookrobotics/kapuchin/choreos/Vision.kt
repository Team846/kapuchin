package com.lynbrookrobotics.kapuchin.choreos

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.control.math.kinematics.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.collector.slider.*
import com.lynbrookrobotics.kapuchin.subsystems.drivetrain.*
import com.lynbrookrobotics.kapuchin.subsystems.lift.*
import com.lynbrookrobotics.kapuchin.subsystems.limelight.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlinx.coroutines.launch

//suspend fun Subsystems.limeLineAlign(
//        limelight: LimelightHardware,
//        slider: CollectorSliderComponent,
//        lift: LiftComponent
//) = startChoreo("Limelight / Line Scanner Alignment") {
//
//    val robotPosition by drivetrain.hardware.position.readEagerly().withoutStamps
//    val targetPosition by limelight.targetPosition.readEagerly().withoutStamps
//    val linePosition by lineScanner.linePosition.readEagerly().withoutStamps
//
//    val liftHeight by lift.hardware.position.readEagerly().withoutStamps
//
//    val transitionPoint = 18.Inch + lineScanner.lookAhead + 1.Foot
//    val targetRange = slider.min..slider.max
//
//    choreography {
//        suspend fun lime() = targetPosition?.takeIf { liftHeight < 1.Inch }?.let { visionSnapshot1 ->
//            val robotSnapshot1 = robotPosition
//            val mtrx = RotationMatrix(robotSnapshot1.bearing)
//            val targetLoc = mtrx rz visionSnapshot1.vector
//            val waypt = robotSnapshot1.vector + targetLoc
//
//            launch { withTimeout(1.Second) { rumble.set(TwoSided(0.Percent, 100.Percent)) } }
//
//            drivetrain.waypoint(
//                    trapezoidalMotionProfile(
//                            6.FootPerSecondSquared,
//                            9.FootPerSecond
//                    ), waypt, transitionPoint
//            )
//        }
//
//        suspend fun line() {
//            launch { slider.trackLine(lineScanner, electrical) }
//            drivetrain.lineActiveTracking(
//                    2.FootPerSecond, targetRange, lineScanner
//            )
//        }
//
//        if (linePosition != null)
//            line()
//        else {
//            lime()
//            line()
//        }
//        freeze()
//    }
//}
//
//suspend fun LimelightHardware.perpendicularAlign(
//        drivetrain: DrivetrainComponent,
//        tolerance: Angle = 10.Degree
//) = startChoreo("Perpendicular align") {
//    val robotPosition by drivetrain.hardware.position.readEagerly().withoutStamps
//    val targetPosition by targetPosition.readEagerly().withoutStamps
//
//    val farEndPt = 3.Foot
//    val closeEndPt = 2.Foot
//
//    choreography {
//        targetPosition?.let { visionSnapshot1 ->
//            val robotSnapshot1 = robotPosition
//            val mtrx = RotationMatrix(robotSnapshot1.bearing)
//            val targetLoc = mtrx rz visionSnapshot1.vector
//
//            if (visionSnapshot1.bearing.abs < tolerance) {
//                val perpPt = mtrx rz UomVector(
//                        closeEndPt * sin(0.Degree),
//                        closeEndPt * cos(0.Degree)
//                )
//
//                val waypt = robotSnapshot1.vector + targetLoc - perpPt
//
//                drivetrain.waypoint(
//                        trapezoidalMotionProfile(
//                                0.5.FootPerSecondSquared,
//                                3.FootPerSecond
//                        ), waypt, 4.Inch
//                )
//            } else {
//                val farPerpPt = mtrx rz UomVector(
//                        farEndPt * sin(visionSnapshot1.bearing),
//                        farEndPt * cos(visionSnapshot1.bearing)
//                )
//
//                val waypt = robotSnapshot1.vector + targetLoc - farPerpPt
//
//                drivetrain.waypoint(
//                        trapezoidalMotionProfile(
//                                0.5.FootPerSecondSquared,
//                                3.FootPerSecond
//                        ), waypt, 4.Inch
//                )
//
//                drivetrain.turn(
//                        robotSnapshot1.bearing + visionSnapshot1.bearing,
//                        tolerance / 2
//                )
//            }
//        }
//
//        drivetrain.visionSnapshotTracking(1.FootPerSecond, this@perpendicularAlign)
//    }
//}
//
//suspend fun DrivetrainComponent.visionSnapshotTracking(speed: Velocity, limelight: LimelightHardware) = startRoutine("Vision snapshot tracking") {
//    val targetAngle by limelight.targetAngle.readOnTick.withoutStamps
//    val robotPosition by hardware.position.readOnTick.withoutStamps
//    val uni = UnicycleDrive(this@visionSnapshotTracking, this@startRoutine)
//
//    val target = targetAngle?.let { it + robotPosition.bearing }
//
//    controller {
//        if (target != null) {
//            val (targs, _) = uni.speedAngleTarget(speed, target)
//
//            val nativeL = hardware.conversions.nativeConversion.native(targs.left)
//            val nativeR = hardware.conversions.nativeConversion.native(targs.right)
//
//            TwoSided(
//                    VelocityOutput(hardware.escConfig, velocityGains, nativeL),
//                    VelocityOutput(hardware.escConfig, velocityGains, nativeR)
//            )
//        } else null
//    }
//}
//
//suspend fun DrivetrainComponent.visionActiveTracking(motionProfile: (Length) -> Velocity, limelight: LimelightHardware, tolerance: Length) = startRoutine("Vision snapshot tracking") {
//    val targetAngle by limelight.targetAngle.readOnTick.withoutStamps
//    val targetPosition by limelight.targetPosition.readOnTick.withoutStamps
//    val robotPosition by hardware.position.readOnTick.withoutStamps
//    val uni = UnicycleDrive(this@visionActiveTracking, this@startRoutine)
//
//    controller {
//        targetAngle?.let { targetSnapshot ->
//            val distance = targetPosition!!.vector.abs
//
//            val (targs, _) = uni.speedAngleTarget(motionProfile(distance), targetSnapshot + robotPosition.bearing)
//
//            val nativeL = hardware.conversions.nativeConversion.native(targs.left)
//            val nativeR = hardware.conversions.nativeConversion.native(targs.right)
//
//            TwoSided(
//                    VelocityOutput(hardware.escConfig, velocityGains, nativeL),
//                    VelocityOutput(hardware.escConfig, velocityGains, nativeR)
//            ).takeIf {
//                distance > tolerance
//            }
//        }
//    }
//}

suspend fun DrivetrainComponent.lineActiveTracking(speed: Velocity, targetRange: ClosedRange<Length>, lineScanner: LineScannerHardware) = startRoutine("Point with line scanner") {
    val linePosition by lineScanner.linePosition.readOnTick.withoutStamps
    val uni = UnicycleDrive(this@lineActiveTracking, this@startRoutine)

    var targetLinePosition: Length? = null

    controller {
        if (targetLinePosition == null) {
            linePosition?.let { lineSnapshot ->
                targetLinePosition = lineSnapshot cap targetRange
                log(Debug) { "Holding line at ${(targetLinePosition ?: 0.Inch).Inch withDecimals 2} inches" }
            }
            TwoSided(VelocityOutput(hardware.escConfig, velocityGains,
                    hardware.conversions.nativeConversion.native(speed))
            )
        } else linePosition?.let { lineSnapshot ->
            val targetA = atan((targetLinePosition ?: 0.Inch) / lineScanner.lookAhead)
            val currentA = atan(lineSnapshot / lineScanner.lookAhead)
            val errorA = -(targetA - currentA)

            val (targetL, targetR) = uni.speedTargetAngleError(speed, errorA)

            val nativeL = hardware.conversions.nativeConversion.native(targetL)
            val nativeR = hardware.conversions.nativeConversion.native(targetR)

            TwoSided(
                    VelocityOutput(hardware.escConfig, velocityGains, nativeL),
                    VelocityOutput(hardware.escConfig, velocityGains, nativeR)
            )
        }
    }
}