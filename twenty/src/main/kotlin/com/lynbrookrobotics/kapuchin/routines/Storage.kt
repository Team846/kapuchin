package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.subsystems.storage.*
import info.kunalsheth.units.generated.*

suspend fun CarouselComponent.set(target: DutyCycle) = startRoutine("Set") {
    controller { PercentOutput(hardware.escConfig, target) }
}

suspend fun CarouselComponent.spinToCollectPosition() = startRoutine("Spin to Collect Position") {
    val magazine by hardware.magazine.readOnTick.withoutStamps
    val position by hardware.position.readOnTick.withoutStamps

    controller {
        val target = magazine.withIndex()
                .map { (i, slotHasBall) -> Pair(360.Degree / 5 * i, slotHasBall) }
                .filter { (_, slotHasBall) -> !slotHasBall }
                .minBy { (angle, _) ->
                    // https://stackoverflow.com/a/7869457/7267809
                    val a = angle - position
                    a + if (a > 180.Degree) (-360).Degree else if (a < 180.Degree) 360.Degree else 0.Degree
                }!!
                .first
        PositionOutput(hardware.escConfig, TODO("Position gains for carousel"), target.Turn)
    }
}

suspend fun FeederRollerComponent.set(target: DutyCycle) = startRoutine("Set") {
    controller { PercentOutput(hardware.escConfig, target) }
}