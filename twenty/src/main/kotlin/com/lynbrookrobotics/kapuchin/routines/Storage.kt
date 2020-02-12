package com.lynbrookrobotics.kapuchin.routines

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

        // not marking otherwise in case they're just not visible
        if (colorSensor == Colors.Yellow.name) {
            hardware.magazineState[slotAtCollect] = true
        }

        // calculate the closest empty slot
        val target = magazine.withIndex()
                .map { (i, slotHasBall) -> Pair(360.Degree / 5 * i, slotHasBall) }
                .filter { (_, slotHasBall) -> !slotHasBall }
                .minBy { (angle, _) ->
                    // https://stackoverflow.com/a/7869457/7267809
                    val a = angle - position
                    a + if (a > 180.Degree) (-360).Degree else if (a < 180.Degree) 360.Degree else 0.Degree
                }
                ?.first
        if (target == null) {
            TODO("Vibrate the controller")
            null
        } else {
            PositionOutput(hardware.escConfig, TODO("Position gains for carousel"), target.Turn)
        }
    }
}

suspend fun FeederRollerComponent.set(target: DutyCycle) = startRoutine("Set") {
    controller { PercentOutput(hardware.escConfig, target) }
}