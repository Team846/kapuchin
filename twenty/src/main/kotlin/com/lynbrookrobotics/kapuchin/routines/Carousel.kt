package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.subsystems.shooter.*
import com.lynbrookrobotics.kapuchin.subsystems.storage.*
import info.kunalsheth.units.generated.*

suspend fun CarouselComponent.collect(state: CarouselMagazineState) = startRoutine("Collect") {
    val position by hardware.position.readOnTick.withoutStamps

    controller {
    }
}

//suspend fun FeederRollerComponent.set(target: DutyCycle) = startRoutine("Set") {
//    controller { PercentOutput(hardware.escConfig, target) }
//}