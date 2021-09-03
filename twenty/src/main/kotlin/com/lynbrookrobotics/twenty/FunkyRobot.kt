package com.lynbrookrobotics.twenty

import com.lynbrookrobotics.kapuchin.logging.*
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
import java.io.*
import kotlin.system.measureTimeMillis

fun main() {
    printBuildInfo()
    RobotBase.startRobot(::FunkyRobot)
}

class FunkyRobot : RobotBase() {
    override fun startCompetition() {
        // Initialize preferences
        Field

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
        println("Starting event loop...")
        while (true) {
            Thread.yield()
            m_ds.waitForData(eventLoopPeriod.Second)

            val eventLoopTime = measureTimeMillis {
                EventLoop.tick(currentTime)
            }.milli(Second)

            if (eventLoopTime > eventLoopPeriod * 5)
                println("Overran event loop by ${(eventLoopTime - eventLoopPeriod) withDecimals 3}")

//            val file = File("/home/lvuser/loopTimes.tsv")
//            val fr = FileWriter(file, true)
//            val br = BufferedWriter(fr)
//            val pr = PrintWriter(br)
//            pr.println("Time remaining on loop: ${eventLoopPeriod - eventLoopTime}")
//            pr.close()
//            br.close()
//            fr.close()

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