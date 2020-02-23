package com.lynbrookrobotics.kapuchin

import com.lynbrookrobotics.kapuchin.choreos.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
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
    printRunID()
    RobotBase.startRobot(::FunkyRobot)
}

class FunkyRobot : RobotBase() {
    override fun startCompetition() {
        println("Initializing hardware...")

        Compressor()
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
                        subsystems.auto()
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

private fun printRunID() {
    val file = File("/home/lvuser/run_id")
    val runId = try {
        file.readText().trim().toInt() + 1
    } catch (e: Exception) {
        System.err.println(e)
        -1
    }
    println("Episode ${runId} - Rohan Awakens")
    file.writeText(runId.toString())
}