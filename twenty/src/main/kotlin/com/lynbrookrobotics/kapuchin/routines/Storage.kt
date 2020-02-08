package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.storage.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

suspend fun CarouselComponent.spin(target: DutyCycle) = startRoutine("Spin") {
    controller { PercentOutput(hardware.escConfig, target) }
}

suspend fun CarouselComponent.spinToEmptySlot() = startRoutine("Spin to Empty Slot") {
    val magazine by hardware.magazine.readOnTick.withoutStamps
    val angle by hardware.angle.readOnTick.withoutStamps

    val target = magazine.filter { !it }.withIndex().map { (i, _) -> 360.Degree * i / 5 }.minBy { abs(angle - it) }!!

    controller { PositionOutput(hardware.escConfig, hardware.positionGains, target.Degree) }
}

suspend fun CarouselComponent.spinFullSlotToShooter() = startRoutine("Spin to Full Slot") {
    val magazine by hardware.magazine.readOnTick.withoutStamps
    val turretLocation by pref(27, Degree)

    val target = magazine.filter { it }.withIndex().map { (i, _) -> 360.Degree * i / 5 }.minBy { abs(turretLocation - it) }!!

    controller { PositionOutput(hardware.escConfig, hardware.positionGains, target.Degree) }
}

suspend fun FeederRollerComponent.spin(target: AngularVelocity) = startRoutine("Spin") {
    controller { VelocityOutput(hardware.escConfig, hardware.escGains, target.Rpm) }
}