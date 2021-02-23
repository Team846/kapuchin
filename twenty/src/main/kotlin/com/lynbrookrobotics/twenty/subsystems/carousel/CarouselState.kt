package com.lynbrookrobotics.twenty.subsystems.carousel

import com.lynbrookrobotics.kapuchin.logging.*
import info.kunalsheth.units.generated.*

class CarouselState(component: Named) : Named by Named("State", component) {

    var balls = 0
        private set
    val maxBalls = 5

    fun intake(): Angle? {
        if (balls == maxBalls) return null
        return (balls + 1).CarouselSlot
    }

    fun shootSetup(): Angle? {
        if (balls == 0) return null
        if (balls == maxBalls) return 0.5.CarouselSlot
        return (balls + 0.5).CarouselSlot
    }

    fun shoot(): Angle? {
        if (balls == 0) return null
        return (balls - 0.5).CarouselSlot
    }

    fun push(ballsToIncrement: Int = 1) {
        balls += ballsToIncrement
    }

    fun pop() {
        balls--
    }

    override fun toString() = "$balls"
}
