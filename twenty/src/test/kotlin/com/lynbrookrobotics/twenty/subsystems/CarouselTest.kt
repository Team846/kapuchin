package com.lynbrookrobotics.twenty.subsystems

import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.twenty.subsystems.carousel.CarouselState
import info.kunalsheth.units.generated.*
import kotlin.test.Test

class CarouselTest : Named by Named("Carousel Test") {

    private val carousel = CarouselState(this)

    @Test
    fun `carousel rotate when loaded`() {

        println(carousel.loadBallAngle(0.Degree))
        val angle = carousel.loadBallAngle(72.Degree)

        if (angle != null) {
            assert(angle == 144.Degree)
        }
    }

    @Test
    fun `rotate for shooter setup`() {
        carousel.loadBallAngle(0.Degree)
        carousel.loadBallAngle(72.Degree)
        val angle = (carousel.moveToShootingPos(144.Degree))

        assert(angle == 0.Degree)
    }

    @Test
    fun `first shot ball angle`() {
        carousel.loadBallAngle(0.Degree)
        carousel.loadBallAngle(72.Degree)
        carousel.moveToShootingPos(144.Degree)
        val angle = (carousel.shootBallAngle(0.Degree))

        if (angle != null) {
            assert(angle == 36.Degree)
        }
    }

    @Test
    fun `shoot again`() {
        carousel.loadBallAngle(0.Degree)
        carousel.loadBallAngle(72.Degree)
        carousel.moveToShootingPos(144.Degree)
        carousel.shootBallAngle(0.Degree)
        val angle = (carousel.shootBallAngle(36.Degree))

        if (angle != null) {
            assert(angle == 108.Degree)
        }
    }
}
