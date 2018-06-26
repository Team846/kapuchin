package com.lynbrookrobotics.kapuchin.tests.hardware

import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.tests.`is equal to?`
import info.kunalsheth.units.generated.Ampere
import info.kunalsheth.units.generated.Percent
import kotlin.test.Test

class OffloadedTest {
    @Test
    fun `LazyOffloadedOutputWriter should only write if it has to`() {
        data class State(
                var wroteKp: Boolean = false,
                var wroteKi: Boolean = false,
                var wroteKd: Boolean = false,
                var wroteKf: Boolean = false,
                var wroteVel: Boolean = false,
                var wrotePos: Boolean = false,
                var wrotePer: Boolean = false,
                var wroteCur: Boolean = false
        )

        var state = State()

        val lazyWriter = LazyOffloadedGainWriter(
                writeKp = { state.wroteKp = true },
                writeKi = { state.wroteKi = true },
                writeKd = { state.wroteKd = true },
                writeKf = { state.wroteKf = true },
                writeVelocity = { state.wroteVel = true },
                writePosition = { state.wrotePos = true },
                writePercent = { state.wrotePer = true },
                writeCurrent = { state.wroteCur = true }
        )

        lazyWriter(VelocityOutput(OffloadedPidGains(1.0, 2.0, 3.0, 4.0), 5.0))
        state `is equal to?` State(true, true, true, true, true)
        state = State()

        repeat(3) {
            lazyWriter(VelocityOutput(OffloadedPidGains(1.0, 2.0, 3.0, 4.0), 5.0))
            state `is equal to?` State(wroteVel = true)
            state = State()
        }

        lazyWriter(VelocityOutput(OffloadedPidGains(1.0, 2.0, 3.0), 5.0))
        state `is equal to?` State(wroteKf = true, wroteVel = true)
        state = State()

        repeat(3) {
            lazyWriter(VelocityOutput(OffloadedPidGains(1.0, 2.0, 3.0), 5.0))
            state `is equal to?` State(wroteVel = true)
            state = State()
        }

        lazyWriter(PositionOutput(OffloadedPidGains(1.0, 2.0, 3.0), 5.0))
        state `is equal to?` State(wrotePos = true)
        state = State()

        repeat(3) {
            lazyWriter(PositionOutput(OffloadedPidGains(1.0, 2.0, 3.0), 5.0))
            state `is equal to?` State(wrotePos = true)
            state = State()
        }

        lazyWriter(PercentOutput(50.Percent))
        state `is equal to?` State(wrotePer = true)
        state = State()

        repeat(3) {
            lazyWriter(PercentOutput(50.Percent))
            state `is equal to?` State(wrotePer = true)
            state = State()
        }

        lazyWriter(CurrentOutput(20.Ampere))
        state `is equal to?` State(wroteCur = true)
        state = State()

        repeat(3) {
            lazyWriter(CurrentOutput(20.Ampere))
            state `is equal to?` State(wroteCur = true)
            state = State()
        }
    }
}