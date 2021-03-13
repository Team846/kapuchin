package com.lynbrookrobotics.twenty

import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.LogLevel.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import com.lynbrookrobotics.twenty.choreos.journalPath
import edu.wpi.first.hal.HAL
import edu.wpi.first.wpilibj.Compressor
import edu.wpi.first.wpilibj.RobotBase
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.system.measureTimeMillis


fun main() {
    defaultRobotLogger
    printBuildInfo()
    RobotBase.startRobot(::FunkyRobot)
}

class FunkyRobot : RobotBase() {
    override fun startCompetition() {
        GlobalNamed.log(INFO) { "Initializing hardware..." }

        Compressor()
        Field // Initialize preferences
        Subsystems.concurrentInit()
        val subsystems = Subsystems.instance!!

        printKeys()

        GlobalNamed.log(INFO) { "Loading classes..." }
        runBlocking { withTimeout(5.Second) { classPreloading.join() } }

        scope.launch {
            runWhenever(
                { isEnabled && isOperatorControl } to {
                    System.gc()
                    HAL.observeUserProgramTeleop()

                    subsystems.teleop()
                    freeze()
                },
                { isEnabled && isAutonomous } to {
                    System.gc()
                    HAL.observeUserProgramAutonomous()

                    subsystems.auto()
                    freeze()
                },
                { isTest } to {
                    System.gc()

                    launch { subsystems.journalPath() }
                    subsystems.teleop()
                    freeze()
                },
                { isDisabled && !isTest } to {
                    subsystems.warmup()
                    freeze()
                },
            )
        }

        System.gc()
        HAL.observeUserProgramStarting()

        val eventLoopPeriod = 20.milli(Second)
        GlobalNamed.log(INFO) { "Starting event loop..." }
        while (true) {
            Thread.yield()
            m_ds.waitForData(eventLoopPeriod.Second)

            val eventLoopTime = measureTimeMillis {
                EventLoop.tick(currentTime)
            }.milli(Second)

            if (eventLoopTime > eventLoopPeriod * 5)
                GlobalNamed.log(WARN) { "Overran event loop by ${(eventLoopTime - eventLoopPeriod) withDecimals 3}" }
        }
    }

    override fun endCompetition() {
        GlobalNamed.log(ERROR) { "Rohan has fallen" }
        runBlocking { EventLogger.flushAll() }
    }
}

val classPreloading = scope.launch {
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
    arrayOf("User", "DateTime", "GitBranch", "GitHash", "GitHasUncommited").forEach {
        val fileContents = Thread.currentThread()
            .contextClassLoader
            .getResourceAsStream("$it.txt")
            ?.bufferedReader()
            ?.readText() ?: "Unknown"

        GlobalNamed.log(INFO) { "$it: $fileContents" }
    }
}