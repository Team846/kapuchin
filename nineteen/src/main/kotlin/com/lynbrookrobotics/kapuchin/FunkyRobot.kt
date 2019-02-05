package com.lynbrookrobotics.kapuchin

import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import edu.wpi.first.hal.HAL
import edu.wpi.first.wpilibj.Preferences2
import edu.wpi.first.wpilibj.RobotBase
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.system.measureTimeMillis

fun main(args: Array<String>) = RobotBase.startRobot(::FunkyRobot)
class FunkyRobot : RobotBase() {
    override fun startCompetition() {
        println("Kapuchin Run ID ${System.currentTimeMillis() / 1000 - 1514000000}")

        val classloading = loadClasses()

        println("Initializing hardware...")
        val subsystems = Subsystems.concurrentInit()

        println("Trimming preferences...")
        trim(Preferences2.getInstance().table)

        runBlocking {
            println("Loading classes...")
            withTimeout(5.Second) { classloading.join() }
        }

        HAL.observeUserProgramStarting()

        val eventLoopPeriod = 20.milli(Second)

        val doNothing = scope.launch { }
        var currentJob: Job = doNothing

        println("Starting event loop...")
        while (true) {
            Thread.yield()
            m_ds.waitForData(eventLoopPeriod.Second)

            val eventLoopTime = measureTimeMillis { EventLoop.tick(currentTime) }.milli(Second)
            if (eventLoopTime > eventLoopPeriod * 2) println("Overran event loop")

            if (!currentJob.isActive) {
                System.gc()

                currentJob = subsystems::teleop runWhile { isEnabled && isOperatorControl }
                        ?: subsystems::followWaypoints runWhile { isEnabled && isAutonomous }
                                ?: subsystems::warmup runWhile { isDisabled }
                                ?: doNothing
            }
        }
    }

    private fun loadClasses() = scope.launch {
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
}