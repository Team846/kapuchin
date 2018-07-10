package com.lynbrookrobotics.kapuchin.tests.subsystems

import com.lynbrookrobotics.kapuchin.hardware.HardwareInit.Companion.hardw
import com.lynbrookrobotics.kapuchin.tests.hardware.TSH
import org.junit.Test

class SubsystemHardwareTest {

    private fun rte(): Nothing = throw RuntimeException("Hello World!")

    @Test(expected = RuntimeException::class)
    fun `initialization fails on exception during hardware initialization`() {
        object : TSH<Nothing, Nothing>("SubsystemHardwareTest Hardware") {
            val brokenHardware by hardw { Any().also { rte() } }
                    .configure { /**/ }
                    .verify("Should fail before verification") { false }
        }
    }

    @Test(expected = RuntimeException::class)
    fun `initialization fails on exception during hardware configuration`() {
        object : TSH<Nothing, Nothing>("SubsystemHardwareTest Hardware") {
            val brokenHardware by hardw { Any() }
                    .configure { rte() }
                    .verify("Should fail before verification") { false }
        }
    }

    @Test(expected = IllegalStateException::class)
    fun `initialization fails on false hardware verification`() {
        object : TSH<Nothing, Nothing>("SubsystemHardwareTest Hardware") {
            val brokenHardware by hardw { Any() }
                    .configure { /*do something here*/ }
                    .verify("Intentionally broken hardware") { false }
        }
    }

    @Test
    fun `initialization succeeds on true hardware verification and no exceptions`() {
        object : TSH<Nothing, Nothing>("SubsystemHardwareTest Hardware") {
            val workingHardware1 by hardw { Any() }
                    .configure { /*do something here*/ }
                    .verify("Hardware1 is working") { true }
            val workingHardware2 by hardw { Any() }
                    .configure { /*do something here*/ }
                    .verify("Hardware2 is working") { true }
            val workingHardware3 by hardw { Any() }
                    .configure { /*do something here*/ }
                    .verify("Hardware3 is working") { true }
        }
    }
}