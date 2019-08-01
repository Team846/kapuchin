package com.lynbrookrobotics.kapuchin

import com.lynbrookrobotics.kapuchin.choreos.*
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.kinematics.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.routines.*
import info.kunalsheth.units.generated.*
import kotlinx.coroutines.launch

suspend fun Subsystems.cargoShipSandstorm() = startChoreo("Rocket Sandstorm") {
    val habToCloseCargo = loadTrajectory("hab_to_close_cargo.tsv", 60.Percent)
    val closeCargoToLoading = loadTrajectory("close_cargo_to_loading.tsv", 60.Percent)
    val loadingToMiddleCargo = loadTrajectory("loading_to_middle_cargo.tsv", 60.Percent)

    val interrupt by driver.interruptAuto.readEagerly(0.Second).withoutStamps

    val currentPosition by drivetrain.hardware.position.readEagerly(0.Second).withoutStamps
    val linePosition by lineScanner.linePosition.readEagerly(0.Second).withoutStamps

    System.gc()

    choreography {
        runWhile({ !interrupt }) {
            launch { lift?.set(lift.panelLowRocket) }
            drivetrain.followTrajectory(20.Inch, 8.Inch, 5.FootPerSecondSquared, habToCloseCargo)
            launch { collectorSlider?.trackLine(lineScanner, electrical) }
            launch { drivetrain.openLoop(30.Percent) }
            lift?.set(lift.cargoShip)
            deployCargo(true)
            lift?.set(lift.panelCollect)

            val origin1 = currentPosition.vector +
                    (RotationMatrix(currentPosition.bearing) rz UomVector(
                            x = -(linePosition ?: 0.Inch),
                            y = 0.Inch
                    ))
            val bearing1 = currentPosition.bearing

            withTimeout(0.5.Second) { drivetrain.openLoop(-30.Percent) }

            drivetrain.followTrajectory(20.Inch, 8.Inch, 5.FootPerSecondSquared, closeCargoToLoading,
                    Position(origin1.x, origin1.y, bearing1)
            )
//            launch { trackLine() }
//            launch { drivetrain.openLoop(30.Percent) }
//            lift?.set(lift.panelMidRocket)
//            deployPanel()
//            withTimeout(0.5.Second) { drivetrain.openLoop(-30.Percent) }
//
//            val origin2 = currentPosition.vector +
//                    (RotationMatrix(currentPosition.bearing) rz UomVector(
//                            x = -(linePosition ?: 0.Inch),
//                            y = 0.Inch
//                    ))
//
//            drivetrain.followTrajectory(20.Inch, 2.Inch, 5.FootPerSecondSquared, loadingToMiddleCargo,
//                    Position(origin2.x, origin2.y, currentPosition.bearing)
//            )

//            withTimeout(2.Second) {
//                launch { trackLine() }
//                lift?.set(lift.panelHighRocket)
//            }
//            withTimeout(1.Second) { deployPanel() }

//            val mtrx = RotationMatrix(startingPosition.bearing)
//            val newLocation = startingPosition.vector + (mtrx rz UomVector(linePosition ?: 0.Foot, 0.Foot, 0.Foot))
//            val newOrigin = Position(newLocation.x, newLocation.y, startingPosition.bearing)

//            withTimeout(1.Second) { drivetrain.openLoop(-50.Percent) }
//
//            drivetrain.followTrajectory(20.Inch, 4.Inch, 5.FootPerSecondSquared, rocketToLoading, newOrigin)

            freeze()
        }
        log(Debug) { "Interrupted" }
    }
}

fun Subsystems.loadTrajectory(name: String, performance: Dimensionless): List<TimeStamped<Waypt>> {
    val waypts = Thread.currentThread()
            .contextClassLoader
            .getResourceAsStream("com/lynbrookrobotics/kapuchin/paths/$name")
            .bufferedReader()
            .lineSequence()
            .drop(1)
            .map { it.split('\t') }
            .map { it.map { tkn -> tkn.trim() } }
            .map { Waypt(it[1].toDouble().Foot, it[2].toDouble().Foot) /*stampWith it[0].toDouble().Second*/ }
            .toList()

    return pathToTrajectory(
            waypts,
            performance,
            drivetrain.maxSpeed,
            drivetrain.maxOmega
    )
}