package com.lynbrookrobotics.kapuchin

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.drivetrain.*
import edu.wpi.first.hal.HAL
import info.kunalsheth.units.generated.*
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

object Subsystems {

    lateinit var drivetrain: DrivetrainComponent
    lateinit var driverHardware: DriverHardware
    lateinit var electricalHardware: ElectricalSystemHardware

    fun concurrentInit() = runBlocking {
        val drivetrainAsync = async { DrivetrainComponent(DrivetrainHardware()) }
        val driverAsync = async { DriverHardware() }
        val electricalAsync = async { ElectricalSystemHardware() }

        drivetrain = drivetrainAsync.await()
        driverHardware = driverAsync.await()
        electricalHardware = electricalAsync.await()
    }

    fun init() {
        drivetrain = DrivetrainComponent(DrivetrainHardware())
        driverHardware = DriverHardware()
        electricalHardware = ElectricalSystemHardware()
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