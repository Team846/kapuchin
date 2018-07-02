package com.lynbrookrobotics.kapuchin

import com.lynbrookrobotics.kapuchin.routines.teleop.teleop
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.drivetrain.DrivetrainComponent
import com.lynbrookrobotics.kapuchin.subsystems.drivetrain.DrivetrainHardware
import com.lynbrookrobotics.kapuchin.timing.EventLoop
import com.lynbrookrobotics.kapuchin.timing.currentTime
import edu.wpi.first.wpilibj.RobotBase
import edu.wpi.first.wpilibj.hal.HAL
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withTimeout

class FunkyRobot : RobotBase() {
    override fun startCompetition() {
        val electricalHardware = ::ElectricalSystemHardware.safeCall()

        val shooterHardware = ::ShooterHardware.safeCall()
        val shooterComponent = shooterHardware with electricalHardware creates ::ShooterComponent

        val driverHardware = ::DriverHardware.safeCall()

        val drivetrainHardware = ::DrivetrainHardware.safeCall()
        val drivetrainComponent = drivetrainHardware with driverHardware creates ::DrivetrainComponent

        HAL.observeUserProgramStarting()

        launch {
            withTimeout(10000) {
                drivetrainComponent?.teleop { false }
            }
        }

        while (true) {
            m_ds.waitForData()
            EventLoop.tick(currentTime)
        }
    }
}