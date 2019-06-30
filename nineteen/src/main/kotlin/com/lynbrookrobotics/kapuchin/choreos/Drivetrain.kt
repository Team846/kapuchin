package com.lynbrookrobotics.kapuchin.choreos

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.control.math.kinematics.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.collector.*
import com.lynbrookrobotics.kapuchin.subsystems.drivetrain.*
import com.lynbrookrobotics.kapuchin.subsystems.lift.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

suspend fun Subsystems.drivetrainTeleop() = startChoreo("Drivetrain teleop") {

    val autoAlign by driver.autoAlign.readEagerly().withoutStamps

    choreography {
        try {
            launch {
                launchWhenever(
                        { drivetrain.routine == null } to choreography {
                            drivetrain.teleop(driver)
                        }
                )
            }
            launch {
                runWhenever(
                        { autoAlign } to choreography {
                            //if (limelight != null && collectorSlider != null && lift != null) {
//limeLineAlign(limelight, collectorSlider, lift)
//}
                            launch { collectorSlider?.trackLine(lineScanner, electrical) }
                            drivetrain.lineActiveTracking(
                                    3.FootPerSecond,
                                    collectorSlider
                                            ?.run { (min - 0.5.Inch)..(max + 0.5.Inch) }
                                            ?: -5.Inch..5.Inch,
                                    lineScanner
                            )
                        }
                )
            }
            freeze()
        } catch (t: Throwable) {
            log(Error, t) { "The drivetrain teleop control is exiting!!!" }
            throw t
        }
    }
}

suspend fun Subsystems.limeLineAlign(
        limelight: Limelight,
        slider: CollectorSlider,
        lift: Lift
) = startChoreo("Limelight / Line Scanner Alignment") {

    val robotPosition by drivetrain.hardware.position.readEagerly().withoutStamps
    val targetPosition by limelight.targetPosition.readEagerly().withoutStamps
    val linePosition by lineScanner.linePosition.readEagerly().withoutStamps

    val liftHeight by lift.hardware.position.readEagerly().withoutStamps

    val transitionPoint = 18.Inch + lineScanner.lookAhead + 1.Foot
    val targetRange = slider.min..slider.max

    choreography {
        suspend fun lime() = targetPosition?.takeIf { liftHeight < 1.Inch }?.let { visionSnapshot1 ->
            val robotSnapshot1 = robotPosition
            val mtrx = RotationMatrix(robotSnapshot1.bearing)
            val targetLoc = mtrx rz visionSnapshot1.vector
            val waypt = robotSnapshot1.vector + targetLoc

            launch { withTimeout(1.Second) { rumble.set(TwoSided(0.Percent, 100.Percent)) } }

            drivetrain.waypoint(
                    trapezoidalMotionProfile(
                            6.FootPerSecondSquared,
                            9.FootPerSecond
                    ), waypt, transitionPoint
            )
        }

        suspend fun line() {
            launch { slider.trackLine(lineScanner, electrical) }
            drivetrain.lineActiveTracking(
                    2.FootPerSecond, targetRange, lineScanner
            )
        }

        if (linePosition != null)
            line()
        else {
            lime()
            line()
        }
        freeze()
    }
}

suspend fun Limelight.perpendicularAlign(
        drivetrain: Drivetrain,
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

suspend fun journal(dt: DrivetrainHardware, ptDistance: Length = 6.Inch) = startChoreo("Journal") {

    val pos by dt.position.readEagerly(2.milli(Second)).withStamps
    val log = File("/tmp/journal.tsv").printWriter().also {
        it.println("time\tx\ty")
    }

    val startingLoc = pos.y.vector
    val startingRot = RotationMatrix(-pos.y.bearing)

    var last = pos.y

    choreography {
        log.use {
            while (isActive) {
                val (t, loc) = pos
                val (x, y) = startingRot rz (loc.vector - startingLoc)

                if (distance(loc.vector, last.vector) > ptDistance) {
                    it.println("${t.Second}\t${x.Foot}\t${y.Foot}")
                    last = loc
                }

                delay(100.milli(Second))
            }

            val (t, loc) = pos
            val (x, y) = startingRot rz (loc.vector - startingLoc)
            it.println("${t.Second}\t${x.Foot}\t${y.Foot}")
        }
    }
}

