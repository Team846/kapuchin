package com.lynbrookrobotics.twenty.subsystems.carousel

import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlin.math.roundToInt

class CarouselState(component: Named) : Named by Named("State", component) {
    private val internal = arrayOf(false, false, false, false, false)
    var state = 0
    private var firstShot = true

    private fun index(robotBearing: Angle) =
        Math.floorMod(robotBearing.CarouselSlot.roundToInt(), size)

    operator fun get(robotBearing: Angle) = internal[index(robotBearing)]

    operator fun set(robotBearing: Angle, newState: Boolean) {
        val index = index(robotBearing)
        log(Debug) { "Setting state of slot #$index @ ${robotBearing.Degree withDecimals 0}˚ from ${internal[index]} to $newState" }
        if (internal[index] && newState) log(Error) { "#$index was assumed to be full. Setting to full again." }
        internal[index] = newState
    }

    fun loadBallAngle(current: Angle): `∠`? {
        if (state == 5) return null
        if (!firstShot) {
            firstShot = true
            return (state * 1.CarouselSlot + current - 0.5.CarouselSlot)
        }
        firstShot = true
        state++
        return (1.CarouselSlot + current)
    }

    fun moveToShootingPos(current: Angle): `∠`? {
        if (state == 5) {
            log(Warning) { "Empty carousel" }
            return null
        }
        return ((5 - state) * 1.CarouselSlot + current)
    }

    fun shootBallAngle(current: Angle): `∠`? {
        if (state == 0) {
            log(Warning) { "No ball was there to shoot" }
            return null
        }
        state--
        if (firstShot) {
            firstShot = false
            return (0.5.CarouselSlot + current)
        }
        return (1.CarouselSlot + current)

    }

    val ammo get() = internal.count { it }
    val size = internal.size

    override fun toString() = internal.joinToString(
        prefix = "CarouselState(", separator = ", ", postfix = ")"
    )
}