package com.lynbrookrobotics.twenty.choreos

import com.ctre.phoenix.motorcontrol.NeutralMode.Brake
import com.ctre.phoenix.motorcontrol.NeutralMode.Coast
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.twenty.Subsystems
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.TimeUnit

suspend fun Subsystems.journalPath(cut: Length = 3.Inch) = startChoreo("Journal Path") {

    val pos by drivetrain.hardware.position.readEagerly(2.milli(Second)).withoutStamps

    val logDir = "/home/lvuser/"
    val logPath = "${journalId}.tsv"

    val log = File(logPath).printWriter().also {
        it.println("x\ty")
        it.println("0.0\t0.0")
    }

    val startingLoc = pos.vector
//    val startingRot = RotationMatrix(-(if (Auto.recordReverse) 180.Degree `coterminal +` pos.bearing else pos.bearing))
    val startingRot = RotationMatrix(-pos.bearing)
    var last = pos

    val drivetrainEscs = with(drivetrain.hardware) { setOf(leftMasterEsc, rightMasterEsc, leftSlaveEsc, rightSlaveEsc) }

    choreography {
        try {
            drivetrainEscs.forEach { it.setNeutralMode(Coast) }
            while (isActive) {
                val (x, y) = startingRot rz (pos.vector - startingLoc)

                if (distance(pos.vector, last.vector) > cut) {
                    log.println("${x.Foot}\t${y.Foot}")
                    last = pos
                }

                delay(50.milli(Second))
            }
        } finally {
            val (x, y) = startingRot rz (pos.vector - startingLoc)
            log.println("${x.Foot}\t${y.Foot}")
            log.close()

            scope.launch {
                @Suppress("BlockingMethodInNonBlockingContext")
                ProcessBuilder("sync").directory(File(logDir)).inheritIO().start().waitFor(5, TimeUnit.SECONDS)
                log(Debug) { "Done syncing $logDir." }
            }
            drivetrainEscs.forEach { it.setNeutralMode(Brake) }
        }
    }
}