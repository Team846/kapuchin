package com.lynbrookrobotics.twenty.subsystems.carousel

import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlin.math.roundToInt

class CarouselState(component: Named) : Named by Named("State", component) {
    private val internal = arrayOf(false, false, false, false, false)
    var state = 0
    var firstShot = true

    private fun index(robotBearing: Angle) =
        Math.floorMod(robotBearing.CarouselSlot.roundToInt(), size)

    operator fun get(robotBearing: Angle) = internal[index(robotBearing)]

    operator fun set(robotBearing: Angle, newState: Boolean) {
        val index = index(robotBearing)
        log(Debug) { "Setting state of slot #$index @ ${robotBearing.Degree withDecimals 0}˚ from ${internal[index]} to $newState" }
        if (internal[index] && newState) log(Error) { "#$index was assumed to be full. Setting to full again." }
        internal[index] = newState
    }

    /**
     * @param current - Carousels current angle position
     * @return - null if carousel is full,
     *           otherwise angle to move to after picking ball
     */
    fun loadBallAngle(current: Angle): `∠`? {
        if (state == 5) return null
        if (!firstShot) {
            firstShot = true
            return (state * 72.Degree + current - 36.Degree)%(360.Degree)
        }
        firstShot = true
        state++
        return (72.Degree + current)%(360.Degree)
    }

    /**
     * @param current - Carousels current angle position
     * @return - 0 if carousel is full since there's nothing to do,
     *           otherwise the angle to move to so that the slot at shooter is empty and slot before is full
     */
    fun moveToShootingPos(current: Angle): `∠` {
        if (state == 5) {
            log(Warning) { "Empty carousel" }
            return 0.Degree
        }
        return ((5 - state) * 72.Degree + current)%(360.Degree)
    }

    /**
     * @param current - Carousels current angle position
     * @return - null if carousel is empty but angle to move to shoot a ball if carousel isn't empty
     */
    fun shootBallAngle(current: Angle): `∠`? {
        if (state == 0) {
            log(Warning) { "No ball was there to shoot" }
            return null
        }
        state--
        if (firstShot) {
            firstShot = false
            return (36.Degree + current)%(360.Degree)
        }
        return (72.Degree + current)%(360.Degree)

    }

    val ammo get() = internal.count { it }
    val size = internal.size

    override fun toString() = internal.joinToString(
        prefix = "CarouselState(", separator = ", ", postfix = ")"
    )
}