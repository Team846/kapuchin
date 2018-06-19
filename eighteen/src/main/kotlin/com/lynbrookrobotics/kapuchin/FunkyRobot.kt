package com.lynbrookrobotics.kapuchin

import com.lynbrookrobotics.kapuchin.delegates.WithEventLoop
import com.lynbrookrobotics.kapuchin.subsystems.ElectricalSystemComponent
import com.lynbrookrobotics.kapuchin.subsystems.ElectricalSystemHardware
import com.lynbrookrobotics.kapuchin.subsystems.ShooterComponent
import com.lynbrookrobotics.kapuchin.subsystems.ShooterHardware
import edu.wpi.first.wpilibj.RobotBase
import edu.wpi.first.wpilibj.hal.HAL

class FunkyRobot : RobotBase() {
    override fun startCompetition() {

        val electrical = ::ElectricalSystemComponent dependsOn ::ElectricalSystemHardware
        val shooter = ::ShooterComponent dependsOn (::ShooterHardware to electrical)

        HAL.observeUserProgramStarting()

        while (true) {
            m_ds.waitForData()
            WithEventLoop.update()
        }
    }
}