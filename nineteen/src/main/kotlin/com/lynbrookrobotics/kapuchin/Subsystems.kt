package com.lynbrookrobotics.kapuchin

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.hardware.HardwareInit.Companion.crashOnFailure
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.drivetrain.*
import edu.wpi.first.hal.HAL
import info.kunalsheth.units.generated.*
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

object Subsystems {

    lateinit var drivetrain: DrivetrainComponent private set
    lateinit var driverHardware: DriverHardware private set
    lateinit var electricalHardware: ElectricalSystemHardware private set

    fun concurrentInit() = runBlocking {
        val drivetrainAsync = async { DrivetrainComponent(DrivetrainHardware()) }
        val driverAsync = async { DriverHardware() }
        val electricalAsync = async { ElectricalSystemHardware() }

        suspend fun t(f: suspend () -> Unit) = try {
            f()
        } catch (t: Throwable) {
            if (crashOnFailure) throw t else Unit
        }

        t { drivetrain = drivetrainAsync.await() }
        t { driverHardware = driverAsync.await() }
        t { electricalHardware = electricalAsync.await() }
    }

    fun init() {
        fun t(f: () -> Unit) = try {
            f()
        } catch (t: Throwable) {
            if (crashOnFailure) throw t else Unit
        }

        t { drivetrain = DrivetrainComponent(DrivetrainHardware()) }
        t { driverHardware = DriverHardware() }
        t { electricalHardware = ElectricalSystemHardware() }
    }

    suspend fun teleop() {
        runAll(
                { drivetrain.teleop(driverHardware) },
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