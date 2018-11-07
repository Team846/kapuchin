package com.lynbrookrobotics.kapuchin

import com.lynbrookrobotics.kapuchin.routines.Routine.Companion.runWhile
import com.lynbrookrobotics.kapuchin.timing.EventLoop
import com.lynbrookrobotics.kapuchin.timing.currentTime
import edu.wpi.first.wpilibj.RobotBase
import edu.wpi.first.wpilibj.hal.HAL
import info.kunalsheth.units.generated.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import com.lynbrookrobotics.kapuchin.timing.coroutine

class FunkyRobot : RobotBase() {
    override fun startCompetition() {
        val classloading = loadClasses()

        val subsystems = Subsystems.init()

        runBlocking { classloading.join() }

        HAL.observeUserProgramStarting()

        val eventLoopPeriod = 20.milli(Second)

        val doNothing = coroutine.launch { }
        var currentJob: Job = doNothing

        while (true) {
            m_ds.waitForData(eventLoopPeriod.Second)
            EventLoop.tick(currentTime)

            if (!currentJob.isActive) {
                System.gc()

                currentJob =
                        subsystems::teleop runWhile { isEnabled && isOperatorControl }
                        ?: subsystems::backAndForthAuto runWhile { isEnabled && isAutonomous }
                        ?: doNothing
            }
        }
    }

    private fun loadClasses() = coroutine.launch {
        val classNameRegex = """\[Loaded ([\w.$]+) from .+]""".toRegex()
        Thread.currentThread()
                .contextClassLoader
                .getResourceAsStream("com/lynbrookrobotics/kapuchin/preload")
                .bufferedReader()
                .lineSequence()
                .filter { it.matches(classNameRegex) }
                .map { it.replace(classNameRegex, "$1") }
                .forEach {
                    launch(coroutineContext) {
                        try {
                            Class.forName(it)
                        } catch (t: Throwable) {
                        }
                    }
                }
    }
}