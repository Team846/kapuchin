package com.lynbrookrobotics.kapuchin.tests.subsystems

import com.lynbrookrobotics.kapuchin.hardware.HardwareInit.Companion.hardw
import com.lynbrookrobotics.kapuchin.subsystems.SubsystemHardware
import com.lynbrookrobotics.kapuchin.timing.Priority
import info.kunalsheth.units.generated.Second
import org.junit.Test

class SubsystemHardwareTest {

    private abstract class TSH : SubsystemHardware<TSH, Nothing>() {
        override val priority = Priority.RealTime
        override val period = 2.Second
        override val syncThreshold = 0.5.Second
        override val subsystemName = "Test Subsystem Hardware"
    }

    @Test(expected = IllegalStateException::class)
    fun `initialization fails on exception during hardware initialization`() {
        class SH : TSH() {
            val workingHardware1 by hardw { Any() }
            val workingHardware2 by hardw { Any() }
            val brokenHardware by hardw { Any().also { error("Intentionally broken hardware") } }
                    .configure { /**/ }
                    .verify("Should fail before verification") { true }
        }
        SH()
    }

    @Test(expected = IllegalStateException::class)
    fun `initialization fails on exception during hardware configuration`() {
        class SH : TSH() {
            val workingHardware1 by hardw { Any() }
            val workingHardware2 by hardw { Any() }
            val brokenHardware by hardw { Any() }
                    .configure { error("Intentionally broken hardware") }
                    .verify("Should fail before verification") { true }
        }
        SH()
    }

    @Test(expected = IllegalStateException::class)
    fun `initialization fails on false hardware verification`() {
        class SH : TSH() {
            val workingHardware1 by hardw { Any() }
            val workingHardware2 by hardw { Any() }
            val brokenHardware by hardw { Any() }
                    .configure { /*do something here*/ }
                    .verify("Intentionally broken hardware") { false }
        }
        SH()
    }

    @Test
    fun `initialization succeeds on true hardware verification and no exceptions`() {
        class SH : TSH() {
            val workingHardware1 by hardw { Any() }
                    .configure { /**/ }
                    .verify("Hardware1 is working") { true }
            val workingHardware2 by hardw { Any() }
                    .configure { /**/ }
                    .verify("Hardware2 is working") { true }
            val workingHardware3 by hardw { Any() }
                    .configure { /*do something here*/ }
                    .verify("Hardware3 is working") { true }
        }
        SH()
    }
}