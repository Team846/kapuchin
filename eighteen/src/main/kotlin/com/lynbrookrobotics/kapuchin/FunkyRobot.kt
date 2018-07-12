package com.lynbrookrobotics.kapuchin

import com.lynbrookrobotics.kapuchin.routines.Routine.Companion.runWhile
import com.lynbrookrobotics.kapuchin.timing.EventLoop
import com.lynbrookrobotics.kapuchin.timing.currentTime
import edu.wpi.first.wpilibj.RobotBase
import edu.wpi.first.wpilibj.hal.HAL
import info.kunalsheth.units.generated.Second
import info.kunalsheth.units.generated.milli
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch

class FunkyRobot : RobotBase() {
    override fun startCompetition() {
        loadClasses()

        val subsystems = Subsystems.init()

        HAL.observeUserProgramStarting()

        val eventLoopPeriod = 20.milli(::Second)

        val doNothing = launch { }
        var currentJob: Job = doNothing

        while (true) {
            m_ds.waitForData(eventLoopPeriod.Second)
            EventLoop.tick(currentTime)

            if (!currentJob.isActive) {
                System.gc()

                currentJob =
                        subsystems::teleop runWhile { isEnabled && isOperatorControl }
                        ?: doNothing
            }
        }
    }

    private fun loadClasses() {
        val classNameRegex = """\[Loaded ([\w.$]+) from .+]""".toRegex()
        Thread.currentThread()
                .contextClassLoader
                .getResourceAsStream("com/lynbrookrobotics/kapuchin/preload")
                .bufferedReader()
                .lineSequence()
                .filter { it.matches(classNameRegex) }
                .map { it.replace(classNameRegex, "$1") }
                .forEach {
                    try {
                        val c = Class.forName(it)
                        println("Loaded ${c.simpleName} class")
                    } catch (t: Throwable) {
                        println("Could not load $it")
                    }
                }
    }
}