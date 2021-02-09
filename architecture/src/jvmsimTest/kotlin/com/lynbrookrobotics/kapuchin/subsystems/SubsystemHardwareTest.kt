package com.lynbrookrobotics.kapuchin.subsystems

import com.lynbrookrobotics.kapuchin.hardware.*
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
    fun `initialization succeeds with otherwise on exception during hardware initialization`() {
        object : TSH<Nothing, Nothing>("SubsystemHardwareTest Hardware") {
            val brokenHardware by hardw { Any().also { rte() } }
                .configure { /**/ }
                .verify("Should fail before verification") { false }
                .otherwise(hardw { Any() })
        }
    }

    @Test
    fun `initialization succeeds with otherwise on exception during hardware configuration`() {
        object : TSH<Nothing, Nothing>("SubsystemHardwareTest Hardware") {
            val brokenHardware by hardw { Any() }
                .configure { rte() }
                .verify("Should fail before verification") { false }
                .otherwise(hardw { Any() })
        }
    }

    @Test
    fun `initialization succeeds with otherwise on false hardware verification`() {
        object : TSH<Nothing, Nothing>("SubsystemHardwareTest Hardware") {
            val brokenHardware by hardw { Any() }
                .configure { /*do something here*/ }
                .verify("Intentionally broken hardware") { false }
                .otherwise(hardw { Any() })
        }
    }

    @Test
    fun `initialization succeeds with otherwise on exception during preceeding hardware initializations`() {
        object : TSH<Nothing, Nothing>("SubsystemHardwareTest Hardware") {
            val brokenHardware by hardw { Any().also { rte() } }
                .configure { /**/ }
                .verify("Should fail before verification") { false }
                .otherwise(hardw { Any().also { rte() } })
                .otherwise(hardw { Any() }.configure { rte() })
                .otherwise(hardw { Any() }.verify("Intentionally broken hardware") { false })
                .otherwise(hardw { Any() })
        }
    }

    @Test
    fun `initialization succeeds with otherwise on exception during preceeding hardware configurations`() {
        object : TSH<Nothing, Nothing>("SubsystemHardwareTest Hardware") {
            val brokenHardware by hardw { Any() }
                .configure { rte() }
                .verify("Should fail before verification") { false }
                .otherwise(hardw { Any().also { rte() } })
                .otherwise(hardw { Any() }.configure { rte() })
                .otherwise(hardw { Any() }.verify("Intentionally broken hardware") { false })
                .otherwise(hardw { Any() })
        }
    }

    @Test
    fun `initialization succeeds with otherwise on preceeding false hardware verifications`() {
        object : TSH<Nothing, Nothing>("SubsystemHardwareTest Hardware") {
            val brokenHardware by hardw { Any() }
                .configure { /*do something here*/ }
                .verify("Intentionally broken hardware") { false }
                .otherwise(hardw { Any().also { rte() } })
                .otherwise(hardw { Any() }.configure { rte() })
                .otherwise(hardw { Any() }.verify("Intentionally broken hardware") { false })
                .otherwise(hardw { Any() })
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