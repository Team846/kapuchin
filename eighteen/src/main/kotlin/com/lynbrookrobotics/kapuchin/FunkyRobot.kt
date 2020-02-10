package com.lynbrookrobotics.kapuchin

import com.lynbrookrobotics.kapuchin.routines.Routine.Companion.runWhile
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import edu.wpi.first.hal.HAL
import edu.wpi.first.wpilibj.RobotBase
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) = RobotBase.startRobot(::FunkyRobot)
class FunkyRobot : RobotBase() {
    override fun startCompetition() {
        println("Kapuchin Run ID ${System.currentTimeMillis() / 1000 - 1514000000}")

        val classloading = loadClasses()

        val subsystems = Subsystems.init()

        runBlocking { classloading.join() }

        HAL.observeUserProgramStarting()

        val eventLoopPeriod = 20.milli(Second)

        val doNothing = scope.launch { }
        var currentJob: Job = doNothing

        while (true) {
            Thread.yield()
            m_ds.waitForData(eventLoopPeriod.Second)

            EventLoop.tick(currentTime)

            if (!currentJob.isActive) {
                currentJob =
                        subsystems::teleop runWhile { isEnabled && isOperatorControl }
                                ?: subsystems::backAndForthAuto runWhile { isEnabled && isAutonomous }
                                        ?: doNothing
            }
        }
    }

    override fun endCompetition() {}

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