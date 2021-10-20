package com.lynbrookrobotics.twenty.subsystems.carousel

import com.lynbrookrobotics.kapuchin.logging.*
import info.kunalsheth.units.generated.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

// http://svn.lynbrookrobotics.com/cad21/trunk/Users/Andy%20Min/CarouselStatePositions.pdf
class CarouselState(component: Named) : Named by Named("State", component) {

    private val mutex = Mutex()
    var balls = 0
        private set
    private val maxBalls = 5

    suspend fun intakeAngle(): Angle? = mutex.withLock {
        if (balls == maxBalls) return null
        return balls.CarouselSlot
    }

    suspend fun shootInitialAngle(): Angle? = mutex.withLock {
        if (balls == 0) return null
        return (balls - 1).CarouselSlot
    }

    suspend fun push(count: Int = 1) = mutex.withLock { balls += count }

    suspend fun pop() = mutex.withLock { balls-- }

    suspend fun clear() = mutex.withLock { balls = 0 }

    override fun toString() = "CarouselState($balls/$maxBalls)"
}
