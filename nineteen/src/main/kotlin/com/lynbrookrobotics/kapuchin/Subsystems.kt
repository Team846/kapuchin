package com.lynbrookrobotics.kapuchin

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.hardware.HardwareInit.Companion.crashOnFailure
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.driver.*
import com.lynbrookrobotics.kapuchin.subsystems.drivetrain.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import com.lynbrookrobotics.kapuchin.timing.Priority.*
import edu.wpi.first.hal.HAL
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

object Subsystems : Named by Named("subsystems") {

    val pneumaticTicker = ticker(Low, 100.milli(Second), "Stupid Ticker")
    val uiBaselineTicker = ticker(Lowest, 500.milli(Second), "Stupidest Ticker")

    lateinit var drivetrain: DrivetrainComponent private set

    lateinit var teleop: TeleopComponent private set
    lateinit var driver: DriverHardware private set
    lateinit var operator: OperatorHardware private set
    lateinit var leds: LedHardware private set

    lateinit var electrical: ElectricalSystemHardware private set

    fun concurrentInit() = runBlocking {
        val drivetrainAsync = async { DrivetrainComponent(DrivetrainHardware()) }

        val driverAsync = async { DriverHardware() }
        val operatorAsync = async { OperatorHardware() }
        val ledsAsync = async { LedHardware() }
        val teleopAsync = async {
            TeleopComponent(TeleopHardware(
                    driverAsync.await(), operatorAsync.await(), ledsAsync.await()
            ))
        }


        val electricalAsync = async { ElectricalSystemHardware() }

        suspend fun t(f: suspend () -> Unit) = try {
            f()
        } catch (t: Throwable) {
            if (crashOnFailure) throw t else Unit
        }

        t { drivetrain = drivetrainAsync.await() }

        t { teleop = teleopAsync.await() }
        t { driver = driverAsync.await() }
        t { operator = operatorAsync.await() }
        t { leds = ledsAsync.await() }

        t { electrical = electricalAsync.await() }
    }

    fun init() {
        fun t(f: () -> Unit) = try {
            f()
        } catch (t: Throwable) {
            if (crashOnFailure) throw t else Unit
        }

        t { drivetrain = DrivetrainComponent(DrivetrainHardware()) }

        t { driver = DriverHardware() }
        t { operator = OperatorHardware() }
        t { leds = LedHardware() }
        t { teleop = TeleopComponent(TeleopHardware(driver, operator, leds)) }

        t { electrical = ElectricalSystemHardware() }
    }

    suspend fun teleop() {
        runAll(
                { drivetrain.teleop(driver) },
                {
                    HAL.observeUserProgramTeleop()
                    System.gc()
                }
        )
    }

    suspend fun warmup() {
        runAll(
                { drivetrain.warmup() }
        )
    }

    suspend fun followWaypoints() {
        drivetrain.waypoint(3.FootPerSecond, UomVector(0.Foot, 5.Foot), 2.Inch)
        delay(1.Second)
        drivetrain.waypoint(3.FootPerSecond, UomVector(5.Foot, 5.Foot), 2.Inch)
        delay(1.Second)
        drivetrain.waypoint(3.FootPerSecond, UomVector(5.Foot, 0.Foot), 2.Inch)
        delay(1.Second)
        drivetrain.waypoint(3.FootPerSecond, UomVector(0.Foot, 0.Foot), 2.Inch)
        delay(1.Second)
    }

    suspend fun backAndForthAuto() {
        //        while (true) {
//            withTimeout(1.Second) {
//                drivetrain.driveStraight(
//                        8.Foot, 0.Degree,
//                        1.Inch, 2.Degree,
//                        2.FootPerSecondSquared,
//                        3.FootPerSecond
//                )
//            }
//
//            delay(1.Second)
//
//            withTimeout(1.Second) {
//                drivetrain.driveStraight(
//                        -8.Foot, 0.Degree,
//                        1.Inch, 2.Degree,
//                        2.FootPerSecondSquared,
//                        3.FootPerSecond
//                )
//            }
//
//            delay(1.Second)
//        }
    }
}