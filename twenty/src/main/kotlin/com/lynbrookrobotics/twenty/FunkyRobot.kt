package com.lynbrookrobotics.twenty

import com.lynbrookrobotics.kapuchin.control.math.drivetrain.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import com.lynbrookrobotics.twenty.choreos.journalPath
import com.lynbrookrobotics.twenty.routines.followTrajectory
import edu.wpi.first.hal.HAL
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
                    System.gc()

                    var time = 0.Second
                    with(subsystems.drivetrain) {
                        suspend fun manual() {
                            val traj = File("/home/lvuser/slalom_975.tsv")
                                .bufferedReader()
                                .lineSequence()
                                .drop(1)
                                .map { it.split('\t') }
                                .map { it.map { tkn -> tkn.trim() } }
                                .map { Waypoint(it[0].toDouble().Foot, it[1].toDouble().Foot) }
                                .toList()
                                .let {
                                    pathToTrajectory(
                                        it,
                                        maxSpeed * speedFactor,
                                        percentMaxOmega * maxOmega * speedFactor,
                                        maxAcceleration
                                    )
                                }

                            time = measureTimeMillis {
                                followTrajectory(
                                    traj,
                                    maxExtrapolate = maxExtrapolate,
                                    safetyTolerance = 3.Foot,
                                    reverse = false,
                                )
                            }.milli(Second)
                        }

                        manual()
                        log(Debug) { "Trajectroy finished: ${time.Second}s" }
                    }

                    freeze()
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
        .getResourceAsStream("com/lynbrookrobotics/twenty/preload")!!
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