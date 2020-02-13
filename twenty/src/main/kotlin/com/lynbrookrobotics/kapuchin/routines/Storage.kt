package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.subsystems.controlpanel.*
import com.lynbrookrobotics.kapuchin.subsystems.storage.*
import info.kunalsheth.units.generated.*

suspend fun CarouselComponent.set(target: DutyCycle) = startRoutine("Set") {
    controller { PercentOutput(hardware.escConfig, target) }
}

suspend fun CarouselComponent.spinToCollectPosition() = startRoutine("Spin to Collect Position") {
    val magazine by hardware.magazine.readOnTick.withoutStamps
    val position by hardware.position.readOnTick.withoutStamps
    val isHallEffect by hardware.isHallEffect.readOnTick.withoutStamps
    val slotAtCollect by hardware.slotAtCollect.readOnTick.withoutStamps
    val colorSensor by hardware.colorSensor.readOnTick.withoutStamps

    controller {
        // reset the carousel encoder on passing the hall effect
        if (isHallEffect) {
            hardware.encoder.position = (360.Degree / 5 * slotAtCollect).Turn
        }

        if (colorSensor == Colors.Yellow.name) {
            hardware.magazineState[slotAtCollect] = true
        } else if (isHallEffect) {
            hardware.magazineState[slotAtCollect] = false
        }

        // calculate the closest empty slot
        val target = magazine.withIndex()
                .filter { (_, slotHasBall) -> !slotHasBall }
                .map { (i, _) -> 360.Degree / 5 * i }
                .minBy { angle ->
                    // https://stackoverflow.com/a/7869457/7267809
                    angle `coterminal -` position
                }!!
        PositionOutput(hardware.escConfig, TODO("Position gains for carousel"), target.Turn)
    }
}

suspend fun FeederRollerComponent.set(target: DutyCycle) = startRoutine("Set") {
    controller { PercentOutput(hardware.escConfig, target) }
}