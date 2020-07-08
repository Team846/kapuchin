package com.lynbrookrobotics.kapuchin.timing

import edu.wpi.first.wpilibj.RobotController
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

actual val currentTime get() = RobotController.getFPGATime().micro(Second)