package com.lynbrookrobotics.kapuchin

import com.lynbrookrobotics.kapuchin.choreos.*
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.control.math.kinematics.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.subsystems.drivetrain.*
import info.kunalsheth.units.generated.*
import kotlinx.coroutines.launch
import java.io.File

suspend fun Subsystems.rocketSandstorm() = startChoreo("Rocket Sandstorm") {
    val habToRocket = loadTrajectory("left_hab_to_rocket", 85.Percent)

    val currentPosition by drivetrain.hardware.position.readEagerly(0.Second).withoutStamps
    val linePosition by lineScanner.linePosition.readEagerly(0.Second).withoutStamps

    System.gc()

    choreography {
//        withTimeout(5.Second) {
            drivetrain.followTrajectory(20.Inch, 5.Inch, 5.FootPerSecondSquared, habToRocket)
//        }

        withTimeout(2.Second) {
            launch { trackLine() }
            delay(0.5.Second)
            deployPanel()
        }

        val mtrx = RotationMatrix(currentPosition.bearing)
        val newLocation = currentPosition.vector + (mtrx rz UomVector(linePosition ?: 0.Foot, 0.Foot, 0.Foot))
        val newOrigin = Position(newLocation.x, newLocation.y, currentPosition.bearing)
        log(Level.Debug) { "new origin @ $newOrigin" }

        withTimeout(0.5.Second) { drivetrain.openLoop(-80.Percent) }

        freeze()
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