package com.lynbrookrobotics.kapuchin

import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.drivetrain.*
import edu.wpi.first.hal.HAL
import info.kunalsheth.units.generated.*
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

data class Subsystems(
        val drivetrain: DrivetrainComponent,
        val driverHardware: DriverHardware,
        val electricalHardware: ElectricalSystemHardware,
        val lineScannerHardware: LineScannerHardware
) {

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

    companion object : Named by Named("Subsystems Initializer") {
        fun concurrentInit() = runBlocking {
            val drivetrain = async { DrivetrainComponent(DrivetrainHardware()) }
            val driver = async { DriverHardware() }
            val electrical = async { ElectricalSystemHardware() }
            val lineScannerHardware = async { LineScannerHardware() }

            Subsystems(
                    drivetrain = drivetrain.await(),
                    driverHardware = driver.await(),
                    electricalHardware = electrical.await(),
                    lineScannerHardware = lineScannerHardware.await()
            )
        }

        fun init(): Subsystems {
            return Subsystems(
                    drivetrain = DrivetrainComponent(DrivetrainHardware()),
                    driverHardware = DriverHardware(),
                    electricalHardware = ElectricalSystemHardware(),
                    lineScannerHardware = LineScannerHardware()
            )
        }
    }
}