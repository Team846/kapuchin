package com.lynbrookrobotics.twenty.subsystems

import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.twenty.subsystems.carousel.CarouselState
import info.kunalsheth.units.generated.*
import kotlin.test.Test

class CarouselTest : Named by Named("Carousel Test") {

    private val carousel = CarouselState(this)

    @Test
    fun `carousel cycle with 3 balls`() {
        assert(carousel.intake() == 72.Degree)
        carousel.push()

        assert(carousel.intake() == 144.Degree)
        carousel.push()

        assert(carousel.intake() == 216.Degree)
        carousel.push()

        assert(carousel.shootSetup() == 252.Degree)

        assert(carousel.shoot() == 180.Degree)
        carousel.pop()

        assert(carousel.shoot() == 108.Degree)
        carousel.pop()

        assert(carousel.shoot() == 36.Degree)
        carousel.pop()
    }

    @Test
    fun `max carousel cycle test`() {
        assert(carousel.intake() == 72.Degree)
        carousel.push()

        assert(carousel.intake() == 144.Degree)
        carousel.push()

        assert(carousel.intake() == 216.Degree)
        carousel.push()

        assert(carousel.intake() == 288.Degree)
        carousel.push()

        assert(carousel.intake() == 360.Degree)
        carousel.push()

        assert(carousel.intake() == null)

        assert(carousel.shootSetup() == 36.Degree)

        assert(carousel.shoot() == 324.Degree)
        carousel.pop()

        assert(carousel.shoot() == 252.Degree)
        carousel.pop()

        assert(carousel.shoot() == 180.Degree)
        carousel.pop()

        assert(carousel.shoot() == 108.Degree)
        carousel.pop()

        assert(carousel.shoot() == 36.Degree)
        carousel.pop()

        assert(carousel.shoot() == null)
    }
}
