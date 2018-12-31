package com.lynbrookrobotics.kapuchin.timing

import edu.wpi.first.wpilibj.RobotController
import info.kunalsheth.units.generated.Second
import info.kunalsheth.units.math.micro
import info.kunalsheth.units.math.nano

actual val currentTime
    get() = try {
        RobotController.getFPGATime().micro(Second)
    } catch (t: Throwable) {
        System.nanoTime().nano(Second)
    }