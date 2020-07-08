package com.lynbrookrobotics.kapuchin

import com.lynbrookrobotics.kapuchin.choreos.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import edu.wpi.first.hal.HAL
import edu.wpi.first.wpilibj.Compressor
import edu.wpi.first.wpilibj.RobotBase
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.system.measureTimeMillis

fun main() {
    printRunID()
    RobotBase.startRobot(::FunkyRobot)
}

class FunkyRobot : RobotBase() {
    override fun startCompetition() {
        println("Initializing hardware...")

        Compressor()
        Field // Initialize preferences
        Subsystems.concurrentInit()
        val subsystems = Subsystems.instance!!

        println("Printing key list to `keylist.txt`...")
        printKeys()

        println("Loading classes...")
        runBlocking { withTimeout(5.Second) { classPreloading.join() } }

        scope.launch {
            runWhenever(
                    { isEnabled && isOperatorControl } to choreography {
                        System.gc()
                        subsystems.teleop()
                    },
                    { isEnabled && isAutonomous } to choreography {
                        System.gc()

                        subsystems.auto()

//                        withTimeout(5.Second) { subsystems.climberWinch?.set(0.Percent) } // should release chode
//                        subsystems.climberWinch?.set(10.Percent) // extend is positive
//                        subsystems.climberWinch?.set(-10.Percent) // extend is positive

//                        subsystems.turret?.rezero(subsystems.electrical)
//                        launch { subsystems.turret?.set(0.Degree, 0.Degree) }
//                        launch { subsystems.flywheel?.set(5000.Rpm)}
//                        launch { subsystems.feederRoller?.set(5000.Rpm) }
//                        launch { subsystems.drivetrain.set(100.Percent) }
//

//                        while (isActive) {
//                            withTimeout(5.Second) {
//                                subsystems.carousel.set(1.CarouselSlot)
//                            }
//                            delay(1.Second)
//                            withTimeout(5.Second) {
//                                subsystems.carousel.set(5.CarouselSlot)
//                            }
//                            delay(1.Second)
//                            withTimeout(5.Second) {
//                                subsystems.carousel.set(-1.CarouselSlot)
//                            }
//                            delay(1.Second)
//                        }

//                        subsystems.carousel.whereAreMyBalls()
//                        println(subsystems.carousel.state.toString())
//                        subsystems.carousel.log(Debug) { subsystems.carousel.state.toString() }
//                        println(subsystems.carousel.state.toString())

//                        launch { subsystems.feederRoller?.set(subsystems.feederRoller.feedSpeed) }
//                        launch { subsystems.flywheel?.set(4000.Rpm) }
//                        delay(2.Second)
//                        launch { subsystems.carousel.whereAreMyBalls() }

//                        subsystems.turret?.rezero(subsystems.electrical)

//                        launch {
//                           subsystems.intakeSlider?.set(IntakeSliderState.Out)
//                        }
//                        launch {
//                            subsystems.intakeRollers?.set(subsystems.intakeRollers.eatSpeed)
//                        }
//
//                        repeat(60) {
//                            withTimeout(1.Second) { subsystems.shooterHood?.set(ShooterHoodState.Up) }
//                            withTimeout(1.Second) { subsystems.shooterHood?.set(ShooterHoodState.Down) }
//                        }

//                        subsystems.carousel.whereAreMyBalls()
//                        subsystems.eat()

//                        subsystems.turret?.rezero(subsystems.electrical)
//                        while (isActive) {
//                            withTimeout(3.Second) { subsystems.turret?.set(-90.Degree, 0.Degree) }
//                            withTimeout(3.Second) { subsystems.turret?.set(0.Degree, 0.Degree) }
//                            withTimeout(3.Second) { subsystems.turret?.set(90.Degree, 0.Degree) }
//                        }

                        freeze()
//                      subsystems.drivetrain.set(0.Percent)
                    },
                    { isDisabled && !isTest } to choreography {
                        subsystems.warmup()
                    },
                    { isTest } to choreography {
                        System.gc()
                        launch { subsystems.journalPath() }
                        subsystems.teleop()
                    }
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
            .getResourceAsStream("com/lynbrookrobotics/kapuchin/preload")!!
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

private fun printRunID() {
    val file = File("/home/lvuser/run_id")
    val runId = try {
        file.readText().trim().toInt() + 1
    } catch (e: Exception) {
        System.err.println(e)
        -1
    }
    println("Episode $runId - Rohan Awakens")
    file.writeText(runId.toString())
}