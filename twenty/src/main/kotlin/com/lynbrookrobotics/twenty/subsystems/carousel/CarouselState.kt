package com.lynbrookrobotics.twenty.subsystems.carousel

import com.lynbrookrobotics.kapuchin.logging.*
import info.kunalsheth.units.generated.*

// http://svn.lynbrookrobotics.com/cad21/trunk/Users/Andy%20Min/CarouselStatePositions.pdf
class CarouselState(component: Named) : Named by Named("State", component) {

    var balls = 0
        private set
    val maxBalls = 5

    fun intakeAngle(): Angle? {
        if (balls == maxBalls) return null
        return balls.CarouselSlot
    }

    fun shootInitialAngle(): Angle? {
        if (balls == 0) return null
        return (balls - 1).CarouselSlot
    }

    fun shootAngle(): Angle? {
        if (balls == 0) return null
        return (balls - 1.5).CarouselSlot
    }

    fun push(count: Int = 1) {
        balls += count
    }

    fun pop() {
        balls--
    }

    fun clear() {
        balls = 0
    }

    override fun toString() = "CarouselState($balls/$maxBalls)"
}
