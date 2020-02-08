package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.subsystems.collector.*
import info.kunalsheth.units.generated.*

suspend fun IntakePivotComponent.set(state: IntakePivotState) = startRoutine("Set") {
    controller { state }
}

suspend fun CollectorRollersComponent.spin(target: OffloadedOutput) = startRoutine("Spin") {
    controller { target }
}

suspend fun CarouselComponent.spin(target: OffloadedOutput) = startRoutine("Spin") {
    controller { target }
}


