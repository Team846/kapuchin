package com.lynbrookrobotics.kapuchin.choreos

import com.ctre.phoenix.motorcontrol.NeutralMode.Brake
import com.ctre.phoenix.motorcontrol.NeutralMode.Coast
import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.routines.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlinx.coroutines.isActive
import java.io.File

suspend fun Subsystems.journalPath(cut: Length = 3.Inch) = startChoreo("Journal Path") {

    val pos by drivetrain.hardware.position.readEagerly(2.milli(Second)).withoutStamps
    val log = File("/home/lvuser/journal.tsv").printWriter().also {
        it.println("x\ty")
        it.println("0.0\t0.0")
    }

    val startingLoc = pos.vector
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

            drivetrainEscs.forEach { it.setNeutralMode(Brake) }
        }
    }
}