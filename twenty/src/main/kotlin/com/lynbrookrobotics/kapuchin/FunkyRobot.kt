package com.lynbrookrobotics.kapuchin

import com.lynbrookrobotics.kapuchin.choreos.*
import com.lynbrookrobotics.kapuchin.control.math.drivetrain.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.subsystems.carousel.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import edu.wpi.first.hal.HAL
import edu.wpi.first.networktables.NetworkTable
import edu.wpi.first.networktables.NetworkTableInstance
import edu.wpi.first.wpilibj.Compressor
import edu.wpi.first.wpilibj.RobotBase
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.system.measureTimeMillis


fun main() {
    printBuildInfo()
    RobotBase.startRobot(::FunkyRobot)
}

class FunkyRobot : RobotBase() {
    override fun startCompetition() {
        println("Initializing hardware...")

        Compressor()
        Field // Initialize preferences
        Subsystems.concurrentInit()
        val subsystems = Subsystems.instance!!

        println("Printing key list to `keylist.txt`...")
        printKeys()

        println("Loading classes...")
        runBlocking { withTimeout(5.Second) { classPreloading.join() } }

        scope.launch {
            runWhenever(
                { isEnabled && isOperatorControl } to choreography {
                    System.gc()
                    subsystems.teleop()
                },
                { isEnabled && isAutonomous } to choreography {
                    subsystems.carousel.rezero()
                    println("zeroed")
                    delay(2.Second)
                    subsystems.carousel.set(0.CarouselSlot)
                    println("init0: ${subsystems.carousel.hardware.position.optimizedRead(
                        currentTime,
                        0.Second
                    ).y.Degree}")
                    subsystems.carousel.hardware.esc.encoder.setPosition(0.0)
                    println("init: ${subsystems.carousel.hardware.position.optimizedRead(
                        currentTime,
                        0.Second
                    ).y.Degree}")
                    delay(2.Second)
                    var time = measureTimeMillis {
                        repeat(5) {
                            subsystems.carousel.set(
                                it.CarouselSlot, 1.Degree
                            )
                            println("slot $it: ${subsystems.carousel.hardware.position.optimizedRead(
                                currentTime,
                                0.Second
                            ).y.Degree}")
                            delay(1.Second)
                        }
                    }
                    println("ASDF: $time")
                    time = measureTimeMillis {
                        subsystems.carousel.set(
                            0.CarouselSlot
                        )
                    }
                    println("ASDF2: $time")
                    freeze()
//                    val speedFactor = 50.Percent
//                    val traj = File("/home/lvuser/0.tsv")
//                        .bufferedReader()
//                        .lineSequence()
//                        .drop(1)
//                        .map { it.split('\t') }
//                        .map { it.map { tkn -> tkn.trim() } }
//                        .map { Waypoint(it[0].toDouble().Foot, it[1].toDouble().Foot) }
//                        .toList()
//                        .let {
//                            pathToTrajectory(
//                                it,
//                                subsystems.drivetrain.maxSpeed * speedFactor,
//                                subsystems.drivetrain.percentMaxOmega * subsystems.drivetrain.maxOmega * speedFactor,
//                                subsystems.drivetrain.maxAcceleration
//                            )
//                        }
//                    subsystems.drivetrain.followTrajectory(traj, 12.Inch, 2.Inch, reverse = false)
//                    freeze()
                },
                { isDisabled && !isTest } to choreography {
                    subsystems.warmup()
                },
                { isTest } to choreography {
                    System.gc()
                    launch { subsystems.journalPath() }
                    subsystems.teleop()
                }
            )
        }

        System.gc()
        HAL.observeUserProgramStarting()

        NetworkTableInstance.getDefault().flush()

        val eventLoopPeriod = 20.milli(Second)
        println("Starting event loop...")
        while (true) {
            Thread.yield()
            m_ds.waitForData(eventLoopPeriod.Second)

            val eventLoopTime = measureTimeMillis {
                EventLoop.tick(currentTime)
            }.milli(Second)

            if (eventLoopTime > eventLoopPeriod * 5)
                println("Overran event loop by ${(eventLoopTime - eventLoopPeriod) withDecimals 3}")
        }
    }

    override fun endCompetition() {
        println("Rohan has fallen")
    }
}

val classPreloading = scope.launch {
    println("Loading classes...")
    val classNameRegex = """\[Loaded ([\w.$]+) from .+]""".toRegex()
    Thread.currentThread()
        .contextClassLoader
        .getResourceAsStream("com/lynbrookrobotics/kapuchin/preload")!!
        .bufferedReader()
        .lineSequence()
        .filter { it.matches(classNameRegex) }
        .map { it.replace(classNameRegex, "$1") }
        .forEach {
            launch {
                try {
                    Class.forName(it)
                } catch (t: Throwable) {
                }
            }
        }
}

private fun printBuildInfo() {
    println("BUILD INFO:")

    arrayOf("User", "DateTime", "GitBranch", "GitHash", "GitHasUncommited").forEach {
        val fileContents = Thread.currentThread()
            .contextClassLoader
            .getResourceAsStream("$it.txt")
            ?.bufferedReader()
            ?.readText() ?: "Unknown"

        println("\t$it: $fileContents")
    }
}