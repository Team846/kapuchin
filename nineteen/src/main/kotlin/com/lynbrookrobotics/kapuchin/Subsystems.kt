package com.lynbrookrobotics.kapuchin

import com.lynbrookrobotics.kapuchin.logging.Named
import com.lynbrookrobotics.kapuchin.routines.Routine.Companion.delay
import com.lynbrookrobotics.kapuchin.routines.Routine.Companion.launchAll
import com.lynbrookrobotics.kapuchin.routines.Routine.Companion.withTimeout
import com.lynbrookrobotics.kapuchin.routines.driveStraight
import com.lynbrookrobotics.kapuchin.routines.teleop
import com.lynbrookrobotics.kapuchin.subsystems.DriverHardware
import com.lynbrookrobotics.kapuchin.subsystems.drivetrain.DrivetrainComponent
import com.lynbrookrobotics.kapuchin.subsystems.drivetrain.DrivetrainHardware
import com.lynbrookrobotics.kapuchin.timing.scope
import edu.wpi.first.hal.HAL
import info.kunalsheth.units.generated.*
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

data class Subsystems(
        val drivetrain: DrivetrainComponent,
        val driverHardware: DriverHardware
) {

    fun teleop() = launchAll(
            { drivetrain.teleop(driverHardware) }
    ).also {
        HAL.observeUserProgramTeleop()
        System.gc()
    }

    fun backAndForthAuto() = scope.launch {
        while (true) {
            withTimeout(1.Second) {
                drivetrain.driveStraight(
                        8.Foot, 0.Degree,
                        1.Inch, 2.Degree,
                        2.FootPerSecondSquared,
                        3.FootPerSecond
                )
            }

            delay(1.Second)

            withTimeout(1.Second) {
                drivetrain.driveStraight(
                        -8.Foot, 0.Degree,
                        1.Inch, 2.Degree,
                        2.FootPerSecondSquared,
                        3.FootPerSecond
                )
            }

            delay(1.Second)
        }
    }.also { HAL.observeUserProgramTeleop() }

    companion object : Named by Named("Subsystems Initializer") {
        fun concurrentInit() = runBlocking {
            val drivetrain = async { DrivetrainComponent(DrivetrainHardware()) }
            val driver = async { DriverHardware() }

            Subsystems(
                    drivetrain = drivetrain.await(),
                    driverHardware = driver.await()
            )
        }

        fun init(): Subsystems {
            return Subsystems(
                    drivetrain = DrivetrainComponent(DrivetrainHardware()),
                    driverHardware = DriverHardware()
            )
        }
    }
}