package com.lynbrookrobotics.kapuchin

import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.WithEventLoop
import edu.wpi.first.wpilibj.RobotBase
import edu.wpi.first.wpilibj.hal.HAL

class FunkyRobot : RobotBase() {
    override fun startCompetition() {

        val electricalHardware = ::ElectricalSystemHardware.safeCall()
        val electricalComponent = electricalHardware creates ::ElectricalSystemComponent

        val shooterHardware = ::ShooterHardware.safeCall()
        val shooterComponent = shooterHardware with electricalHardware creates ::ShooterComponent

        val driverHardware = ::DriverHardware.safeCall()

        val drivetrainHardware = ::DrivetrainHardware.safeCall()
        val drivetrainComponent = drivetrainHardware with driverHardware creates ::DrivetrainComponent

        HAL.observeUserProgramStarting()

        while (true) {
            m_ds.waitForData()
            WithEventLoop.update()
        }
    }
}