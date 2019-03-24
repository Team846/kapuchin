package com.lynbrookrobotics.kapuchin

import com.lynbrookrobotics.kapuchin.choreos.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import edu.wpi.first.hal.HAL
import edu.wpi.first.wpilibj.RobotBase
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.system.measureTimeMillis

fun main(args: Array<String>) {
    println("Kapuchin Run ID ${System.currentTimeMillis() / 1000 - 1514000000}")
    RobotBase.startRobot(::FunkyRobot)
}

class FunkyRobot : RobotBase() {
    override fun startCompetition() {
        println("Initializing hardware...")

        Safeties.init()
        Subsystems.concurrentInit()
        val subsystems = Subsystems.instance!!

        println("Trimming preferences...")
        trim()

        println("Loading classes...")
        runBlocking { withTimeout(10.Second) { classPreloading.join() } }

        scope.launch {
            while (isActive) {
                runWhile({ isEnabled && isOperatorControl }) {
                    subsystems.teleop()
                }

                runWhile({ isEnabled && isAutonomous }) {
                    subsystems.trackLine()
                    //                    subsystems.cargoShipSandstorm()
                    subsystems.teleop()
                }

                runWhile({ isDisabled && !isTest }) {
                    subsystems.warmup()
                }

                runWhile({ isTest }) {
                    launch { journal(subsystems.drivetrain.hardware) }
                    subsystems.teleop()
                }
            }
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
            if (eventLoopTime > eventLoopPeriod * 5) println("Overran event loop by ${eventLoopTime - eventLoopPeriod}")
        }
    }
}

val classPreloading = scope.launch {
    println("Loading classes...")
    val classNameRegex = """\[Loaded ([\w.$]+) from .+]""".toRegex()
    Thread.currentThread()
            .contextClassLoader
            .getResourceAsStream("com/lynbrookrobotics/kapuchin/preload")
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