package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.subsystems.storage.*

suspend fun CarouselComponent.spin(target: OffloadedOutput) = startRoutine("Spin") {
    controller { target }
}

suspend fun FeederRollerComponent.spin(target: OffloadedOutput) = startRoutine("Spin") {
    controller { target }
}