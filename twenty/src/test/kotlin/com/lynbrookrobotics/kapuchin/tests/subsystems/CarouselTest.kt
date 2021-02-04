

package com.lynbrookrobotics.kapuchin.tests.subsystems

import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.subsystems.carousel.*
import info.kunalsheth.units.generated.*
import java.lang.Math.PI
import java.util.Random
import kotlin.reflect.KFunction1
import kotlin.test.Test
import kotlin.test.assertEquals

class CarouselTest : Named by Named("Carousel Test") {

    private val carousel = CarouselState(this)

    @Test
    fun `carousel rotate when loaded`(){

        println(carousel.loadBallAngle(0.Degree))
        val angle = carousel.loadBallAngle(72.Degree)

        if (angle != null) {
            assert(angle == 144.Degree)
        }
    }

    @Test
    fun `rotate for shooter setup`(){
        carousel.loadBallAngle(0.Degree)
        carousel.loadBallAngle(72.Degree)
        val angle = (carousel.moveToShootingPos(144.Degree))

        assert(angle == 360.Degree)
    }

    @Test
    fun `first shot ball angle`(){
        carousel.loadBallAngle(0.Degree)
        carousel.loadBallAngle(72.Degree)
        carousel.moveToShootingPos(144.Degree)
        val angle = (carousel.shootBallAngle(360.Degree))

        if (angle != null) {
            assert(angle == 396.Degree)
        }
    }

    @Test
    fun `shoot again`(){
        carousel.loadBallAngle(0.Degree)
        carousel.loadBallAngle(72.Degree)
        carousel.moveToShootingPos(144.Degree)
        carousel.shootBallAngle(360.Degree)
        val angle = (carousel.shootBallAngle(396.Degree))

        if (angle != null) {
            assert(angle == 468.Degree)
        }
    }



/*
    @Test
    fun `carousel state rotates with position`() {
        val state = CarouselState(this)
        state.assertThroughRotation(false, false, false, false, false)
        state.set(0.CarouselSlot, true)
        state.assertThroughRotation(true, false, false, false, false)
        state.set(4.CarouselSlot, true)
        state.assertThroughRotation(true, false, false, false, true)
        state.set(0.CarouselSlot, false)
        state.assertThroughRotation(false, false, false, false, true)
    }

   *@Test
    fun `carousel detects nearest full slot`() {
        val state = CarouselState(this)
        state.set(1.CarouselSlot, true)

        (-50..50 step 5).map { it.CarouselSlot }.forEach { base ->
            state.assertClosest(base - 4.CarouselSlot, base - 4.CarouselSlot, state::closestFull)
            state.assertClosest(base - 3.CarouselSlot, base - 4.CarouselSlot, state::closestFull)
            state.assertClosest(base - 2.CarouselSlot, base - 4.CarouselSlot, state::closestFull)
            state.assertClosest(base - 1.CarouselSlot, base + 1.CarouselSlot, state::closestFull)
            state.assertClosest(base + 0.CarouselSlot, base + 1.CarouselSlot, state::closestFull)
            state.assertClosest(base + 1.CarouselSlot, base + 1.CarouselSlot, state::closestFull)
            state.assertClosest(base + 2.CarouselSlot, base + 1.CarouselSlot, state::closestFull)
            state.assertClosest(base + 3.CarouselSlot, base + 1.CarouselSlot, state::closestFull)
            state.assertClosest(base + 4.CarouselSlot, base + 6.CarouselSlot, state::closestFull)
        }

        state.set(0.CarouselSlot, true)
        state.set(1.CarouselSlot, false)
        state.set(2.CarouselSlot, true)
        state.set(3.CarouselSlot, true)
        state.set(4.CarouselSlot, true)

        (-50..50 step 5).map { it.CarouselSlot }.forEach { base ->
            state.assertClosest(base - 4.CarouselSlot, base - 5.CarouselSlot, state::closestFull)
            state.assertClosest(base - 3.CarouselSlot, base - 3.CarouselSlot, state::closestFull)
            state.assertClosest(base - 2.CarouselSlot, base - 2.CarouselSlot, state::closestFull)
            state.assertClosest(base - 1.CarouselSlot, base - 1.CarouselSlot, state::closestFull)
            state.assertClosest(base + 0.CarouselSlot, base + 0.CarouselSlot, state::closestFull)
            state.assertClosest(base + 1.CarouselSlot, base + 0.CarouselSlot, state::closestFull)
            state.assertClosest(base + 2.CarouselSlot, base + 2.CarouselSlot, state::closestFull)
            state.assertClosest(base + 3.CarouselSlot, base + 3.CarouselSlot, state::closestFull)
            state.assertClosest(base + 4.CarouselSlot, base + 4.CarouselSlot, state::closestFull)
        }
    }

    @Test
    fun `carousel detects nearest empty slot`() {
        val state = CarouselState(this)
        state.set(1.CarouselSlot, true)

        (-50..50 step 5).map { it.CarouselSlot }.forEach { base ->
            state.assertClosest(base - 4.CarouselSlot, base - 3.CarouselSlot, state::closestEmpty)
            state.assertClosest(base - 3.CarouselSlot, base - 3.CarouselSlot, state::closestEmpty)
            state.assertClosest(base - 2.CarouselSlot, base - 2.CarouselSlot, state::closestEmpty)
            state.assertClosest(base - 1.CarouselSlot, base - 1.CarouselSlot, state::closestEmpty)
            state.assertClosest(base + 0.CarouselSlot, base + 0.CarouselSlot, state::closestEmpty)
            state.assertClosest(base + 1.CarouselSlot, base + 2.CarouselSlot, state::closestEmpty)
            state.assertClosest(base + 2.CarouselSlot, base + 2.CarouselSlot, state::closestEmpty)
            state.assertClosest(base + 3.CarouselSlot, base + 3.CarouselSlot, state::closestEmpty)
            state.assertClosest(base + 4.CarouselSlot, base + 4.CarouselSlot, state::closestEmpty)
        }

        state.set(0.CarouselSlot, true)
        state.set(1.CarouselSlot, false)
        state.set(2.CarouselSlot, true)
        state.set(3.CarouselSlot, true)
        state.set(4.CarouselSlot, true)

        (-50..50 step 5).map { it.CarouselSlot }.forEach { base ->
            state.assertClosest(base - 4.CarouselSlot, base - 4.CarouselSlot, state::closestEmpty)
            state.assertClosest(base - 3.CarouselSlot, base - 4.CarouselSlot, state::closestEmpty)
            state.assertClosest(base - 2.CarouselSlot, base - 4.CarouselSlot, state::closestEmpty)
            state.assertClosest(base - 1.CarouselSlot, base + 1.CarouselSlot, state::closestEmpty)
            state.assertClosest(base + 0.CarouselSlot, base + 1.CarouselSlot, state::closestEmpty)
            state.assertClosest(base + 1.CarouselSlot, base + 1.CarouselSlot, state::closestEmpty)
            state.assertClosest(base + 2.CarouselSlot, base + 1.CarouselSlot, state::closestEmpty)
            state.assertClosest(base + 3.CarouselSlot, base + 1.CarouselSlot, state::closestEmpty)
            state.assertClosest(base + 4.CarouselSlot, base + 6.CarouselSlot, state::closestEmpty)
        }
    }

    val defaultOffsets = listOf(0.1, 0.2, 0.3)
        .map { it.CarouselSlot }
        .flatMap { setOf(+it, -it) }

    fun CarouselState.assert(
        it: Angle,
        s0: Boolean, s1: Boolean, s2: Boolean, s3: Boolean, s4: Boolean
    ) {
        assert(get(it + 0.CarouselSlot) == s0) { "get(${it.Degree withDecimals 0}˚ + 0.CarouselSlot) == $s0" }
        assert(get(it + 1.CarouselSlot) == s1) { "get(${it.Degree withDecimals 0}˚ + 1.CarouselSlot) == $s1" }
        assert(get(it + 2.CarouselSlot) == s2) { "get(${it.Degree withDecimals 0}˚ + 2.CarouselSlot) == $s2" }
        assert(get(it + 3.CarouselSlot) == s3) { "get(${it.Degree withDecimals 0}˚ + 3.CarouselSlot) == $s3" }
        assert(get(it + 4.CarouselSlot) == s4) { "get(${it.Degree withDecimals 0}˚ + 4.CarouselSlot) == $s4" }
    }

    fun CarouselState.assertWithOffsets(
        it: Angle,
        s0: Boolean, s1: Boolean, s2: Boolean, s3: Boolean, s4: Boolean,
        offsets: Collection<Angle> = defaultOffsets
    ) = offsets.forEach { offset -> assert(it + offset, s0, s1, s2, s3, s4) }

    fun CarouselState.assertThroughRotation(
        s0: Boolean, s1: Boolean, s2: Boolean, s3: Boolean, s4: Boolean
    ) = (-50..50 step 5)
        .shuffled(Random(846))
        .map { it.CarouselSlot }
        .forEach { base ->
            assertWithOffsets(base - 6.CarouselSlot, s4, s0, s1, s2, s3)
            assertWithOffsets(base - 5.CarouselSlot, s0, s1, s2, s3, s4)
            assertWithOffsets(base - 4.CarouselSlot, s1, s2, s3, s4, s0)
            assertWithOffsets(base - 3.CarouselSlot, s2, s3, s4, s0, s1)
            assertWithOffsets(base - 2.CarouselSlot, s3, s4, s0, s1, s2)
            assertWithOffsets(base - 1.CarouselSlot, s4, s0, s1, s2, s3)
            assertWithOffsets(base - 0.CarouselSlot, s0, s1, s2, s3, s4)
            assertWithOffsets(base + 0.CarouselSlot, s0, s1, s2, s3, s4)
            assertWithOffsets(base + 1.CarouselSlot, s1, s2, s3, s4, s0)
            assertWithOffsets(base + 2.CarouselSlot, s2, s3, s4, s0, s1)
            assertWithOffsets(base + 3.CarouselSlot, s3, s4, s0, s1, s2)
            assertWithOffsets(base + 4.CarouselSlot, s4, s0, s1, s2, s3)
            assertWithOffsets(base + 5.CarouselSlot, s0, s1, s2, s3, s4)
            assertWithOffsets(base + 6.CarouselSlot, s1, s2, s3, s4, s0)
        }

    fun CarouselState.assertClosest(
        to: Angle, expected: Angle, f: KFunction1<Angle, `∠`?>
    ) = defaultOffsets.forEach { offset ->
        assert(f(to + offset)!! in expected `±` 1.Degree) {
            "${f.name}(${(offset + offset).Degree withDecimals 0}˚)!! == ${f(to)!!.Degree withDecimals 0}˚ != ${expected.Degree withDecimals 0}"
        }
    }*/
}
