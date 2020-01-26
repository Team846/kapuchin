package com.lynbrookrobotics.kapuchin

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.drivetrain.*
import com.lynbrookrobotics.kapuchin.routines.*
import info.kunalsheth.units.generated.*
import java.io.File
import kotlin.math.sin
import kotlin.math.cos
import kotlin.math.PI

fun loadPath(name: String): Path =
        Thread.currentThread()
                .contextClassLoader
                .getResourceAsStream("com/lynbrookrobotics/kapuchin/paths/$name")
                .bufferedReader()
                .lineSequence()
                .drop(1)
                .map { it.split('\t') }
                .map { it.map { tkn -> tkn.trim() } }
                .map { Waypt(it[0].toDouble().Foot, it[1].toDouble().Foot) }
                .toList()

fun loadTempPath(): Path =
        File("/tmp/journal.tsv")
                .bufferedReader()
                .lineSequence()
                .drop(1)
                .map { it.split('\t') }
                .map { it.map { tkn -> tkn.trim() } }
                .map { Waypt(it[0].toDouble().Foot, it[1].toDouble().Foot) }
                .toList()



suspend fun Subsystems.straightLine() = startChoreo("Straight line") {
    val path = nSect(Waypt(0.Foot, 0.Foot), Waypt(0.Foot, 8.Foot), 3.Inch)
    val trajectory = pathToTrajectory(path, 10.FootPerSecond, 1.Radian / Second, 3.FootPerSecondSquared)

    System.gc()

    choreography {
        drivetrain.followTrajectory(trajectory, 1.Inch, 1.Inch)
    }
}

suspend fun Subsystems.circle() = startChoreo("Circle") {
    val path = (0 until 300)
            .map { it / 150.0 }
            .map { Waypt((5 * cos(PI * it) - 5).Foot, (5 *sin(PI * it)).Foot) }

    val trajectory = pathToTrajectory(path, 10.FootPerSecond, 1.Radian / Second, 3.FootPerSecondSquared)

    System.gc()

    choreography {
        drivetrain.followTrajectory(trajectory, 20.Inch, 6.Inch)
    }
}

suspend fun Subsystems.followJournal() = startChoreo("Follow journal") {
    val path = loadTempPath()

    val trajectory = pathToTrajectory(path, 10.FootPerSecond, 1.Radian / Second, 3.FootPerSecondSquared)

    System.gc()

    choreography {
        drivetrain.followTrajectory(trajectory, 20.Inch, 6.Inch)
    }
}
//suspend fun Subsystems.cargoShipSandstorm() = startChoreo("Rocket Sandstorm") {
//    val habToCloseCargo = loadTrajectory("hab_to_close_cargo.tsv", 60.Percent)
//    val closeCargoToLoading = loadTrajectory("close_cargo_to_loading.tsv", 60.Percent)
//    val loadingToMiddleCargo = loadTrajectory("loading_to_middle_cargo.tsv", 60.Percent)
//
//    val interrupt by driver.interruptAuto.readEagerly(0.Second).withoutStamps
//
//    val currentPosition by drivetrain.hardware.position.readEagerly(0.Second).withoutStamps
//    val linePosition by lineScanner.linePosition.readEagerly(0.Second).withoutStamps
//
//    System.gc()
//
//    choreography {
//        runWhile({ !interrupt }) {
//            launch { lift?.set(lift.panelLowRocket) }
//            drivetrain.followTrajectory(20.Inch, 8.Inch, 5.FootPerSecondSquared, habToCloseCargo)
//            launch { collectorSlider?.trackLine(lineScanner, electrical) }
//            launch { drivetrain.openLoop(30.Percent) }
//            lift?.set(lift.cargoCargoShip)
//            deployCargo(true)
//            lift?.set(lift.panelCollect)
//
//            val origin1 = currentPosition.vector +
//                    (RotationMatrix(currentPosition.bearing).rotate(UomVector(
//                            x = -(linePosition ?: 0.Inch),
//                            y = 0.Inch
//                    )))
//            val bearing1 = currentPosition.bearing
//
//            withTimeout(0.5.Second) { drivetrain.openLoop(-30.Percent) }
//
//            drivetrain.followTrajectory(20.Inch, 8.Inch, 5.FootPerSecondSquared, closeCargoToLoading,
//                    Position(origin1.x, origin1.y, bearing1)
//            )
////            launch { trackLine() }
////            launch { drivetrain.openLoop(30.Percent) }
////            lift?.set(lift.panelMidRocket)
////            deployPanel()
////            withTimeout(0.5.Second) { drivetrain.openLoop(-30.Percent) }
////
////            val origin2 = currentPosition.vector +
////                    (RotationMatrix(currentPosition.bearing) rz UomVector(
////                            x = -(linePosition ?: 0.Inch),
////                            y = 0.Inch
////                    ))
////
////            drivetrain.followTrajectory(20.Inch, 2.Inch, 5.FootPerSecondSquared, loadingToMiddleCargo,
////                    Position(origin2.x, origin2.y, currentPosition.bearing)
////            )
//
////            withTimeout(2.Second) {
////                launch { trackLine() }
////                lift?.set(lift.panelHighRocket)
////            }
////            withTimeout(1.Second) { deployPanel() }
//
////            val mtrx = RotationMatrix(startingPosition.bearing)
////            val newLocation = startingPosition.vector + (mtrx rz UomVector(linePosition ?: 0.Foot, 0.Foot, 0.Foot))
////            val newOrigin = Position(newLocation.x, newLocation.y, startingPosition.bearing)
//
////            withTimeout(1.Second) { drivetrain.openLoop(-50.Percent) }
////
////            drivetrain.followTrajectory(20.Inch, 4.Inch, 5.FootPerSecondSquared, rocketToLoading, newOrigin)
//
//            freeze()
//        }
//        log(Debug) { "Interrupted" }
//    }
//}
//