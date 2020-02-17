package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.subsystems.carousel.*
import com.lynbrookrobotics.kapuchin.subsystems.carousel.CarouselMagazineState.Companion.collectSlot

suspend fun CarouselComponent.toCollectPosition(state: CarouselMagazineState) = startRoutine("Collect") {
    val position by hardware.position.readOnTick.withoutStamps
    val target = state.closestOpenSlot(collectSlot)

    controller {

    }
}

//suspend fun FeederRollerComponent.set(target: DutyCycle) = startRoutine("Set") {
//    controller { PercentOutput(hardware.escConfig, target) }
//}