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

    operator fun get(robotBearing: Angle) = internal[index(robotBearing)]

    operator fun set(robotBearing: Angle, newState: Boolean) {
        val index = index(robotBearing)
        log(Debug) { "Setting state of slot #$index @ ${robotBearing.Degree withDecimals 0}˚ from ${internal[index]} to $newState" }
        if (internal[index] && newState) log(Error) { "#$index was assumed to be full. Setting to full again." }
        internal[index] = newState
    }

    //    hardware doesn't support CW & CCW turning
    private fun closest(slot: Angle, bias: Boolean, f: (Angle) -> Boolean): `∠`? {
        val signum = if (bias) -1 else 1
        for (i in 0..2) {
            val offset = i.CarouselSlot * signum
            if (f(slot + offset)) return slot + offset
            if (f(slot - offset)) return slot - offset
        }
        return null
    }

//    private fun closest(slot: Angle, bias: Boolean, f: (Angle) -> Boolean): `∠`? {
//        for (i in 0 until 5) {
//            val position = slot - i.CarouselSlot
//            if (f(position)) return position
//        }
//        return null
//    }

    fun closestEmpty(robotBearing: Angle): Angle? = closest(robotBearing, false) {
        !get(it)
    }?.roundToInt(CarouselSlot)?.CarouselSlot

    fun closestFull(robotBearing: Angle): Angle? = closest(robotBearing, true) {
        get(it)
    }?.roundToInt(CarouselSlot)?.CarouselSlot

    val ammo get() = internal.count { it }
    val size = internal.size

    override fun toString() = internal.joinToString(
        prefix = "CarouselState(", separator = ", ", postfix = ")"
    )
}