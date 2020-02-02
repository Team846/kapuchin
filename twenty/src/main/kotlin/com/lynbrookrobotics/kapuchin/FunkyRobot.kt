package com.lynbrookrobotics.kapuchin

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

const val BASE_PATH = "com/lynbrookrobotics/kapuchin"
const val ID_FILE = "$BASE_PATH/run_id"

fun main() {
    val runId = Thread.currentThread().contextClassLoader.getResource(ID_FILE)?.path?.let {
        val runIdFile = File(it)
        val runId = (runIdFile.readBytes().first() + 1).toByte()
        runIdFile.writeBytes(ByteArray(1) { runId })
        runId
    } ?: -1

    println("Kapuchin Run ID $runId")

    RobotBase.startRobot(::FunkyRobot)
}

class FunkyRobot : RobotBase() {
    override fun startCompetition() {
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
                    { isEnabled && isOperatorControl } to choreography {
                        subsystems.teleop()
                    },
                    { isEnabled && isAutonomous } to choreography {
                        // subsystems.cargoShipSandstorm()
                        subsystems.teleop()
                    },
                    { isDisabled && !isTest } to choreography {
                        subsystems.warmup()
                    },
                    { isTest } to choreography {
                        launch { journal(subsystems.drivetrain.hardware) }
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

    override fun endCompetition() {}
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