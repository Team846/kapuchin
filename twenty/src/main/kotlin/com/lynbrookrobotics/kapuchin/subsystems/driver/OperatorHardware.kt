package com.lynbrookrobotics.kapuchin.subsystems.driver

import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*

class OperatorHardware : RobotHardware<OperatorHardware>() {
    override val name = "Operator"
    override val priority = Priority.RealTime

}
