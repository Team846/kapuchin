package com.lynbrookrobotics.twenty.subsystems

import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.twenty.subsystems.carousel.CarouselSlot
import com.lynbrookrobotics.twenty.subsystems.carousel.CarouselState
import kotlin.test.Test

// http://svn.lynbrookrobotics.com/cad21/trunk/Users/Andy%20Min/CarouselStatePositions.pdf
class CarouselTest : Named by Named("Carousel Test") {

    private val carousel = CarouselState(this)

    @Test
    fun `carousel intake n shoot n`() {
        for (count in 1 until 5) {
            // Intake
            repeat(count) {
                assert(carousel.intakeAngle() == it.CarouselSlot)
                carousel.push()
            }

            assert(carousel.balls == count)

            // Shoot initial
            assert(carousel.shootInitialAngle() == count.CarouselSlot)

            // Shoot
            repeat(count) {
                assert(carousel.shootAngle() == ((count - it) - 0.5).CarouselSlot)
                carousel.pop()
            }

            assert(carousel.balls == 0)
        }
    }

    @Test
    fun `carousel intake returns null when full`() {
        carousel.push(carousel.maxBalls)
        assert(carousel.intakeAngle() == null)
    }

    @Test
    fun `carousel initial returns null when empty`() {
        assert(carousel.shootInitialAngle() == null)
    }

    @Test
    fun `carousel shoot returns null when empty`() {
        assert(carousel.shootAngle() == null)
    }
}
