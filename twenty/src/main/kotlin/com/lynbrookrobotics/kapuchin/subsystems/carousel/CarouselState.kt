package com.lynbrookrobotics.kapuchin.subsystems.carousel

import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlin.math.roundToInt

class CarouselState(component: Named) : Named by Named("State", component) {
    private val internal = arrayOf(false, false, false, false, false)
    var currentSlot = 0

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
     * Rotates the carousel slot once clockwise, does not perform checks for balls
     * @return - Angle to move to in order to rotate one slot
     */
    fun rotateOnce(isIntake: Boolean): `∠` {
        val newSlot = ++currentSlot%5
        internal[newSlot] = isIntake
        if(isIntake) return newSlot.CarouselSlot
        return newSlot.CarouselSlot + 36.Degree
    }


    /**
     * Rotates the carousel such that the first ball (most clockwise from intake side) to 324 degrees
     * so that when shooting, we can rotate in increments of 72 degrees
     * @return - Angle to move to, null if carousel is full
     */
    fun rotateNearestEmpty(): Angle?{
        var isFull = true
        for(i in internal) {
            if(!i){
                isFull = false
                break
            }
        }

        if(isFull) return null
        val ans = ((4 - currentSlot) * 72).Degree + 36.Degree
        currentSlot += (4-currentSlot)
        currentSlot %= 5
        return ans
    }
    
/*
    /**
     * returns the first non empty slot as an angle in the carousel while going only one direction
     */
    fun closestEmpty(): `∠`?{
        for(i in 0..5){
            if(!internal[currentSlot%5]) return currentSlot.CarouselSlot //Check return statement later
            currentSlot++
        }
        return null
    }

    /**
     * returns the first full slot going the opposite direction as `closestEmpty`
     */
    fun closestFull(): `∠`?{
        for(i in 0..5){
            if(internal[currentSlot%5]) return currentSlot.CarouselSlot //Check return statement later
            currentSlot--
       }
        return null
    }
*/



    val ammo get() = internal.count { it }
    val size = internal.size

    override fun toString() = internal.joinToString(
        prefix = "CarouselState(", separator = ", ", postfix = ")"
    )
}