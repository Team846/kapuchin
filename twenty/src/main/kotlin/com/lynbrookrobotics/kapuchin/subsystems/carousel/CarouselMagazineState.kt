package com.lynbrookrobotics.kapuchin.subsystems.carousel

inline class CarouselMagazineState(private val underlying: Int) {
    val fullSlots get() = Integer.bitCount(underlying and full.underlying)
    val emptySlots get() = 5 - fullSlots

    fun closestOpenSlot(center: Int) = closestSlot(center) { !it }
    fun closestClosedSlot(center: Int) = closestSlot(center) { it }
    fun closestSlot(center: Int, f: (Boolean) -> Boolean): Int? {
        for (i in 0..3) when {
            f(this[center + i]) -> return +i
            f(this[center - i]) -> return -i
        }
        return null
    }

    companion object {
        val empty = CarouselMagazineState(0b00000)
        val full = CarouselMagazineState(0b11111)

        const val collectSlot = 0
        const val cwChamber = 0
        const val ccwChamber = 1
    }

    operator fun get(slot: Int) = underlying shr (slot % 5) and 0b1 == 1
    operator fun set(slot: Int, value: Boolean) = CarouselMagazineState(
            (0b1 shl (slot % 5)).let { bit ->
                if (value == true) bit or underlying
                else bit.inv() and underlying
            }
    )

    fun rotateCW(shot: Boolean): CarouselMagazineState {
        var state = this
        if (shot) state = state.set(cwChamber, false)
        state = CarouselMagazineState(state.underlying shl 1)
        state.set(0, state[5])
        return state
    }

    fun rotateCCW(shot: Boolean): CarouselMagazineState {
        var state = this
        if (shot) state = state.set(ccwChamber, false)
        state.set(5, state[0])
        state = CarouselMagazineState(state.underlying shr 1)
        return state
    }

    operator fun component1() = get(0)
    operator fun component2() = get(1)
    operator fun component3() = get(2)
    operator fun component4() = get(3)
    operator fun component5() = get(4)

}