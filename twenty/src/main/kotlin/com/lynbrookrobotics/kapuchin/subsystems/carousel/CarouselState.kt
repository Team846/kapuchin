package com.lynbrookrobotics.kapuchin.subsystems.carousel

import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlin.math.roundToInt

class CarouselState(component: Named) : Named by Named("State", component) {
    private val internal = arrayOf(false, false, false, false, false)

    private fun index(robotBearing: Angle) =
            Math.floorMod(robotBearing.CarouselSlot.roundToInt(), size)

    operator fun get(robotBearing: Angle) = internal.get(index(robotBearing))

    operator fun set(robotBearing: Angle, state: Boolean) {
        val index = index(robotBearing)
        log(Debug) { "Setting state of slot #$index @ ${robotBearing.Degree withDecimals 0}˚ from ${internal.get(index)} to $state" }
        if (internal[index] && state) log(Error) { "#$index was assumed to be full. Setting to full again." }
        internal.set(index, state)
    }

    private fun closest(slot: Angle, bias: Boolean, f: (Angle) -> Boolean): `∠`? {
        val signum = if (bias) -1 else 1
        for (i in 0..2) {
            val offset = i.CarouselSlot * signum
            if (f(slot + offset)) return slot + offset
            if (f(slot - offset)) return slot - offset
        }
        return null
    }

    fun closestEmpty(robotBearing: Angle) = closest(robotBearing, false) {
        !get(it)
    }?.roundToInt(CarouselSlot)

    fun closestFull(robotBearing: Angle) = closest(robotBearing, true) {
        get(it)
    }?.roundToInt(CarouselSlot)

    val ammo get() = internal.count { it }
    val size = internal.size

    override fun toString() = internal.joinToString(
            prefix = "CarouselState(", separator = ", ", postfix = ")"
    )
}