package com.lynbrookrobotics.twenty.subsystems.carousel

import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlin.math.roundToInt

class CarouselState(component: Named) : Named by Named("State", component) {

    var balls = 0
        private set
    val maxBalls = 5
    val noBalls = 0

    fun intake(): Angle? {
        if(balls == maxBalls) return null
        return (balls + 1).CarouselSlot
    }

    fun shootSetup(): Angle? {
        if(balls == noBalls) return null
        return (balls - 0.5).CarouselSlot
    }

    fun shoot(): Angle?{
        if(balls == noBalls) return null;
        return (balls - 0.5).CarouselSlot
    }

    fun push() {
        balls++;
    }

    fun pop() {
        balls--;
    }

    override fun toString() = "$balls"
}
