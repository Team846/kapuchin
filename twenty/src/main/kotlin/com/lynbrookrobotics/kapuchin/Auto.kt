package com.lynbrookrobotics.kapuchin

import com.lynbrookrobotics.kapuchin.choreos.*
import com.lynbrookrobotics.kapuchin.control.math.drivetrain.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.routines.*
import info.kunalsheth.units.generated.*
import kotlinx.coroutines.launch
import java.io.File

private fun loadPath(name: String) = Thread.currentThread()
        .contextClassLoader
        .getResourceAsStream("com/lynbrookrobotics/kapuchin/paths/$name")!!
        .bufferedReader()
        .lineSequence()
        .drop(1)
        .map { it.split('\t') }
        .map { it.map { tkn -> tkn.trim() } }
        .map { Waypoint(it[0].toDouble().Foot, it[1].toDouble().Foot) }
        .toList()

private fun loadTempPath() = File("/home/lvuser/journal.tsv")
        .bufferedReader()
        .lineSequence()
        .drop(1)
        .map { it.split('\t') }
        .map { it.map { tkn -> tkn.trim() } }
        .map { Waypoint(it[0].toDouble().Foot, it[1].toDouble().Foot) }
        .toList()

private fun Subsystems.fastAsFuckLine(dist: Length = 4.Foot): Trajectory =
        pathToTrajectory(
                nSect(Waypoint(0.Foot, 0.Foot), Waypoint(0.Foot, dist), 3.Inch),
                drivetrain.maxSpeed, drivetrain.percentMaxOmega * drivetrain.maxOmega, drivetrain.maxAcceleration
        )

private fun Subsystems.fastAsFuck(name: String): Trajectory =
        pathToTrajectory(
                loadPath(name),
                drivetrain.maxSpeed, drivetrain.percentMaxOmega * drivetrain.maxOmega, drivetrain.maxAcceleration
        )

/**
 * Start from anywhere, shoot3, drive to wall
 */
suspend fun Subsystems.`shoot3 wall`() {
    if (flywheel == null || feederRoller == null) {
        log(Error) { "Need flywheel and feeder to run auto" }
        freeze()
    } else startChoreo("shoot3 wall") {

        val wall = fastAsFuckLine()

        val flywheelSpeed by flywheel.hardware.speed.readEagerly().withoutStamps
        val feederSpeed by feederRoller.hardware.speed.readEagerly().withoutStamps

        choreography {
            runAll(
                    choreography {
                        carousel.rezero()
                        carousel.whereAreMyBalls()
                    },
                    choreography {
                        turret?.rezero(electrical)
                    }
            )

            val aim = launch { visionAim() }
            repeat(carousel.state.ammo) {
                delayUntil { feederRoller.check(feederSpeed) && flywheel.check(flywheelSpeed) }
                fire()
            }
            aim.cancel()

            drivetrain.followTrajectory(wall, 12.Inch, 2.Inch)
        }
    }
}