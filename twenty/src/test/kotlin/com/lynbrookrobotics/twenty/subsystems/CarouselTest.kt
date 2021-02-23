package com.lynbrookrobotics.twenty.subsystems

import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.twenty.subsystems.carousel.CarouselState
import info.kunalsheth.units.generated.*
import kotlin.test.Test

class CarouselTest : Named by Named("Carousel Test") {

    private val carousel = CarouselState(this)

    @Test
    fun `carousel cycle with 3 balls`() {
        assert(carousel.intakeAngle() == 72.Degree)
        carousel.push()

        assert(carousel.intakeAngle() == 144.Degree)
        carousel.push()

        assert(carousel.intakeAngle() == 216.Degree)
        carousel.push()

        assert(carousel.shootInitialAngle() == 252.Degree)

        assert(carousel.shootAngle() == 180.Degree)
        carousel.pop()

        assert(carousel.shootAngle() == 108.Degree)
        carousel.pop()

        assert(carousel.shootAngle() == 36.Degree)
        carousel.pop()
    }

    @Test
    fun `max carousel cycle test`() {
        assert(carousel.intakeAngle() == 72.Degree)
        carousel.push()

        assert(carousel.intakeAngle() == 144.Degree)
        carousel.push()

        assert(carousel.intakeAngle() == 216.Degree)
        carousel.push()

        assert(carousel.intakeAngle() == 288.Degree)
        carousel.push()

        assert(carousel.intakeAngle() == 360.Degree)
        carousel.push()

        assert(carousel.intakeAngle() == null)

        assert(carousel.shootInitialAngle() == 36.Degree)

        assert(carousel.shootAngle() == 324.Degree)
        carousel.pop()

        assert(carousel.shootAngle() == 252.Degree)
        carousel.pop()

        assert(carousel.shootAngle() == 180.Degree)
        carousel.pop()

        assert(carousel.shootAngle() == 108.Degree)
        carousel.pop()

        assert(carousel.shootAngle() == 36.Degree)
        carousel.pop()

        assert(carousel.shootAngle() == null)
    }
}
