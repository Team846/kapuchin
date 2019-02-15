package com.lynbrookrobotics.kapuchin

import com.lynbrookrobotics.kapuchin.routines.Routine.Companion.launchAll
import com.lynbrookrobotics.kapuchin.routines.teleop
import com.lynbrookrobotics.kapuchin.routines.warmup
import com.lynbrookrobotics.kapuchin.subsystems.DriverHardware
import com.lynbrookrobotics.kapuchin.subsystems.ElectricalSystemHardware
import com.lynbrookrobotics.kapuchin.subsystems.drivetrain.DrivetrainComponent
import com.lynbrookrobotics.kapuchin.subsystems.drivetrain.DrivetrainHardware
import com.lynbrookrobotics.kapuchin.subsystems.lift.*
import com.lynbrookrobotics.kapuchin.timing.scope
import edu.wpi.first.hal.HAL
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
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

    fun teleop() = launchAll(
            { drivetrain.teleop(driverHardware) }
    ).also {
        HAL.observeUserProgramTeleop()
        System.gc()
    }

    fun warmup() = launchAll(
            { drivetrain.warmup() }
    ).also {
        HAL.observeUserProgramTeleop()
        System.gc()
    }

    fun backAndForthAuto() = scope.launch {
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
    }.also { HAL.observeUserProgramTeleop() }

}