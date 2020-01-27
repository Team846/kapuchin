package com.lynbrookrobotics.kapuchin

import com.lynbrookrobotics.kapuchin.choreos.*
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.kinematics.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.routines.*
import info.kunalsheth.units.generated.*
import kotlinx.coroutines.launch

suspend fun Subsystems.rightShoot3Collect3Shoot3() = startChoreo("Shoot 3, Collect 3, Shoot 3") {

    val interrupt by driver.interruptAuto.readEagerly(0.Second).withoutStamps

    val currentPosition by drivetrain.hardware.position.readEagerly(0.Second).withoutStamps
    val linePosition by lineScanner.linePosition.readEagerly(0.Second).withoutStamps

    System.gc()

    choreography {
        runWhile({ !interrupt }) {
            freeze()
        }
        log(Debug) { "Interrupted" }
    }
}

suspend fun Subsystems.leftCollect2Shoot5() = startChoreo("Collect 2, Shoot 5") {

    val interrupt by driver.interruptAuto.readEagerly(0.Second).withoutStamps

    val currentPosition by drivetrain.hardware.position.readEagerly(0.Second).withoutStamps
    val linePosition by lineScanner.linePosition.readEagerly(0.Second).withoutStamps

    System.gc()

    choreography {
        runWhile({ !interrupt }) {
            freeze()
        }
        log(Debug) { "Interrupted" }
    }
}

fun Subsystems.loadTrajectory(name: String, performance: Dimensionless): List<TimeStamped<Waypt>> {
    val waypts = Thread.currentThread()
            .contextClassLoader
            .getResourceAsStream("com/lynbrookrobotics/kapuchin/paths/$name")!!
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