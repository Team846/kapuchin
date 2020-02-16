package com.lynbrookrobotics.kapuchin.subsystems.storage

inline class CarouselMagazineState(val underlying: Int) {

    operator fun get(slot: Int) = underlying shr slot and 0b1 == 1
    operator fun component1() = get(0)
    operator fun component2() = get(1)
    operator fun component3() = get(2)
    operator fun component4() = get(3)
    operator fun component5() = get(4)

    companion object {
        const val collectSlot = 0
        const val cwChamber = 0
        const val ccwChamber = 1
    }

    fun set(slot: Int, value: Boolean): CarouselMagazineState =
            if (value == true) CarouselMagazineState(0b1 shl slot or underlying)
            else CarouselMagazineState((0b1 shl slot).inv() and underlying)

    fun rotateCW(shot: Boolean): CarouselMagazineState {
        var state = this
        if(shot) state = state.set(cwChamber, false)
        state = CarouselMagazineState(state.underlying shl 1)
        state.set(0, state[5])
        return state
    }

    fun rotateCCW(shot: Boolean): CarouselMagazineState {
        var state = this
        if(shot) state = state.set(ccwChamber, false)
        state.set(5, state[0])
        state = CarouselMagazineState(state.underlying shr 1)
        return state
    }
}