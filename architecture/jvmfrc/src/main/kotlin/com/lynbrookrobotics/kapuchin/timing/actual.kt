package com.lynbrookrobotics.kapuchin.timing

import edu.wpi.first.wpilibj.RobotController
import info.kunalsheth.units.generated.Second
import info.kunalsheth.units.generated.micro

actual val currentTime get() = RobotController.getFPGATime().micro(Second)