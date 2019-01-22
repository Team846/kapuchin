package com.lynbrookrobotics.kapuchin

import com.lynbrookrobotics.kapuchin.hardware.LimelightHardware
import com.lynbrookrobotics.kapuchin.logging.Named
import com.lynbrookrobotics.kapuchin.routines.Routine.Companion.launchAll
import com.lynbrookrobotics.kapuchin.routines.pointWithLimelight
import com.lynbrookrobotics.kapuchin.routines.teleop
import com.lynbrookrobotics.kapuchin.subsystems.DriverHardware
import com.lynbrookrobotics.kapuchin.subsystems.ElectricalSystemHardware
import com.lynbrookrobotics.kapuchin.subsystems.drivetrain.DrivetrainComponent
import com.lynbrookrobotics.kapuchin.subsystems.drivetrain.DrivetrainHardware
import com.lynbrookrobotics.kapuchin.timing.scope
import edu.wpi.first.hal.HAL
import info.kunalsheth.units.generated.*
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import com.lynbrookrobotics.kapuchin.routines.Routine.Companion.withTimeout

data class Subsystems(
        val drivetrain: DrivetrainComponent,
        val driverHardware: DriverHardware,
        val electricalHardware: ElectricalSystemHardware,
        val limelightHardware: LimelightHardware
) {

    fun teleop() = launchAll(
            { drivetrain.teleop(driverHardware, electricalHardware) }
    ).also {
        HAL.observeUserProgramTeleop()
        System.gc()
    }

    fun limelightTracking() = scope.launch {
        while(true) {
            withTimeout(1.Second) {
                drivetrain.pointWithLimelight(5.Degree, limelightHardware, electricalHardware)
            }
        }
    }.also {
        HAL.observeUserProgramAutonomous()
        System.gc()
    }

    companion object : Named by Named("Subsystems Initializer") {
        fun concurrentInit() = runBlocking {
            val drivetrain = async { DrivetrainComponent(DrivetrainHardware()) }
            val driver = async { DriverHardware() }
            val electrical = async { ElectricalSystemHardware() }
            val limelight = async { LimelightHardware() }

            Subsystems(
                    drivetrain = drivetrain.await(),
                    driverHardware = driver.await(),
                    electricalHardware = electrical.await(),
                    limelightHardware = limelight.await()
            )
        }

        fun init(): Subsystems {
            return Subsystems(
                    drivetrain = DrivetrainComponent(DrivetrainHardware()),
                    driverHardware = DriverHardware(),
                    electricalHardware = ElectricalSystemHardware(),
                    limelightHardware = LimelightHardware()
            )
        }
    }
}