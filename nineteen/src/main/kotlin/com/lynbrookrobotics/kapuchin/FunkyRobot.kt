package com.lynbrookrobotics.kapuchin

import com.lynbrookrobotics.kapuchin.routines.Routine.Companion.runWhile
import com.lynbrookrobotics.kapuchin.routines.Routine.Companion.withTimeout
import com.lynbrookrobotics.kapuchin.timing.clock.EventLoop
import com.lynbrookrobotics.kapuchin.timing.currentTime
import com.lynbrookrobotics.kapuchin.timing.scope
import edu.wpi.first.hal.HAL
import edu.wpi.first.wpilibj.RobotBase
import info.kunalsheth.units.generated.Second
import info.kunalsheth.units.math.milli
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
        val subsystems = Subsystems.init()

        runBlocking {
            println("Loading classes...")
            withTimeout(10.Second) { classloading.join() }
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

                currentJob =
                           subsystems::teleop runWhile { isEnabled && isOperatorControl }
                        ?: subsystems::limelightTracking runWhile { isEnabled && isAutonomous }
                        ?: subsystems::teleop runWhile { isDisabled }
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