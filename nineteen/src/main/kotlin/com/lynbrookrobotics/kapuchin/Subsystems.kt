package com.lynbrookrobotics.kapuchin

import com.lynbrookrobotics.kapuchin.logging.Named
import com.lynbrookrobotics.kapuchin.routines.Routine.Companion.launchAll
import com.lynbrookrobotics.kapuchin.routines.pointWithLineScanner
import com.lynbrookrobotics.kapuchin.routines.teleop
import com.lynbrookrobotics.kapuchin.subsystems.DriverHardware
import com.lynbrookrobotics.kapuchin.subsystems.ElectricalSystemHardware
import com.lynbrookrobotics.kapuchin.subsystems.drivetrain.DrivetrainComponent
import com.lynbrookrobotics.kapuchin.subsystems.drivetrain.DrivetrainHardware
import com.lynbrookrobotics.kapuchin.subsystems.drivetrain.LineScannerHardware
import com.lynbrookrobotics.kapuchin.timing.scope
import edu.wpi.first.hal.HAL
import info.kunalsheth.units.generated.Degree
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

data class Subsystems(
        val drivetrain: DrivetrainComponent,
        val driverHardware: DriverHardware,
        val electricalHardware: ElectricalSystemHardware,
        val lineScannerHardware: LineScannerHardware
) {

    fun teleop() = launchAll(
            { drivetrain.teleop(driverHardware, electricalHardware) }
    ).also {
        HAL.observeUserProgramTeleop()
        System.gc()
    }

    fun lineTracking() = scope.launch {
        while (true)
            drivetrain.pointWithLineScanner(5.Degree, lineScannerHardware, electricalHardware)
    }.also {
        HAL.observeUserProgramAutonomous()
        System.gc()
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