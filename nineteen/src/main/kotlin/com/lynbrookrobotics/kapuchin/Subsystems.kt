package com.lynbrookrobotics.kapuchin

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.driver.*
import com.lynbrookrobotics.kapuchin.subsystems.drivetrain.*
import edu.wpi.first.hal.HAL
import info.kunalsheth.units.generated.*
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.awt.Color

data class Subsystems(
        val drivetrain: DrivetrainComponent,
        val driverHardware: DriverHardware,
        val electricalHardware: ElectricalSystemHardware,
        val lineScannerHardware: LineScannerHardware,
        val limelightHardware: LimelightHardware,
        val leds: LEDLightsComponent
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

    suspend fun rainbowLEDs() {
        leds.rainbow(5.Second)
        delay(30.Second)
    }

    suspend fun cycleLEDs() {
        leds.cycle(50, 1.Second, Color.RED, Color.YELLOW)
        delay(30.Second)
    }

    suspend fun fadeLEDs() {
        leds.fade(2.Second, Color.RED)
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
            val limelight = async { LimelightHardware() }
            val leds = async { LEDLightsComponent(LedHardware()) }

            Subsystems(
                    drivetrain = drivetrain.await(),
                    driverHardware = driver.await(),
                    electricalHardware = electrical.await(),
                    lineScannerHardware = lineScannerHardware.await(),
                    limelightHardware = limelight.await(),
                    leds = leds.await()
            )
        }

        fun init(): Subsystems {
            return Subsystems(
                    drivetrain = DrivetrainComponent(DrivetrainHardware()),
                    driverHardware = DriverHardware(),
                    electricalHardware = ElectricalSystemHardware(),
                    lineScannerHardware = LineScannerHardware(),
                    limelightHardware = LimelightHardware(),
                    leds = LEDLightsComponent(LedHardware())
            )
        }
    }
}