package com.lynbrookrobotics.kapuchin.subsystems.carousel

import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlin.math.roundToInt

class CarouselState(component: CarouselComponent) : Named by Named("State", component) {
    private val internal = arrayOf(false, false, false, false, false)

    private fun index(robotBearing: Angle) =
            Math.floorMod(robotBearing.CarouselSlot.roundToInt(), size)

    operator fun get(robotBearing: Angle) = internal.get(index(robotBearing))

    operator fun set(robotBearing: Angle, state: Boolean) {
        log(Warning) { "Setting state of slot #${robotBearing.CarouselSlot withDecimals 1} to $state" }
        internal.set(index(robotBearing), state)
    }

    private fun iterateAround(slot: Angle) = (0..size / 2)
            .map { it.CarouselSlot }
            .flatMap { setOf(slot + it, slot - it) }

    fun closestEmpty(robotBearing: Angle) = iterateAround(robotBearing)
            .filterNot { get(it) }
            .minBy { it.abs }
            ?.roundToInt(CarouselSlot)

    fun closestFull(robotBearing: Angle) = iterateAround(robotBearing)
            .filter { get(it) }
            .minBy { it.abs }
            ?.roundToInt(CarouselSlot)

    val ammo get() = internal.count { it }
    val size = internal.size

    override fun toString() = internal.joinToString(
            prefix = "CarouselState(", separator = ", ", postfix = ")"
    )
}