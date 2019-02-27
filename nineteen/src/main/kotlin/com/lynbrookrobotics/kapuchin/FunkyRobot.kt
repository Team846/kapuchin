package com.lynbrookrobotics.kapuchin

import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.Error
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

        Safeties.init()

        Subsystems.concurrentInit()

        val subsystems = Subsystems.instance

        Subsystems.uiBaselineTicker.runOnTick { Safeties.currentState().forEach { println(it) } }



        println("Trimming preferences...")
        trim(Preferences2.getInstance().table)

        runBlocking {
            println("Loading classes...")
            withTimeout(.5.Second) { classloading.join() }
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
            if (eventLoopTime > eventLoopPeriod * 5) println("Overran event loop by ${eventLoopTime - eventLoopPeriod}")

            if (!currentJob.isActive) {
                System.gc()

                currentJob = scope.launch {
                    runWhile({ isEnabled && isOperatorControl }, { subsystems?.teleop() })
                    System.gc()

                    runWhile({ isEnabled && isAutonomous }, {
                        subsystems?.drivetrain?.openLoop(30.Percent)
                    })
                    System.gc()

                    runWhile({ isDisabled && !isTest }, { subsystems?.warmup() })
                    System.gc()

                    runWhile({ isTest }, {
                        launch {
                            if (subsystems?.drivetrain != null) {
                                journal(subsystems?.drivetrain.hardware)
                            } else {
                                subsystems?.log(Error) {
                                    "Drivetrain not initialized"
                                }
                            }
                        }
                        subsystems?.teleop()
                    })
                    System.gc()
                }
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