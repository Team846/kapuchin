package com.lynbrookrobotics.kapuchin

import com.lynbrookrobotics.kapuchin.routines.Routine.Companion.launchAll
import com.lynbrookrobotics.kapuchin.routines.teleop.teleop
import com.lynbrookrobotics.kapuchin.timing.EventLoop
import com.lynbrookrobotics.kapuchin.timing.currentTime
import edu.wpi.first.wpilibj.RobotBase
import edu.wpi.first.wpilibj.hal.HAL

class FunkyRobot : RobotBase() {
    override fun startCompetition() {
        loadClasses()

        val subsystems = Subsystems.init()

        System.gc()

        HAL.observeUserProgramStarting()

        subsystems.teleop()

        while (true) {
            m_ds.waitForData()
            EventLoop.tick(currentTime)
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

    fun Subsystems.teleop() = launchAll(
            { forks.teleop(driverHardware) },
            { hooks.teleop(driverHardware, lift) },
            { winch.teleop(driverHardware) },
            { clamp.teleop(driverHardware) },
            { pivot.teleop(driverHardware) },
            { rollers.teleop(driverHardware) },
            { drivetrain.teleop(driverHardware, lift) },
            { lift.teleop(driverHardware) }
    )
}