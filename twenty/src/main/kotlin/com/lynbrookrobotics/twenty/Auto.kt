package com.lynbrookrobotics.twenty

import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.preferences.*
import info.kunalsheth.units.generated.*

object Auto : Named by Named("Auto") {
    val teleopID by pref(0)
    val autoID by pref(0)

    val recordFile: String by pref("0")
    val recordReverse by pref(false)

    val defaultPathConfig by autoPathConfigPref("0")

    object AutoNav : Named by Named("AutoNav", this) {
        val barrel by autoPathConfigPref("barrel")
        val slalom by autoPathConfigPref("slalom")

        // Bounce path files are split into bounce{1,2,3,4}.tsv
        val bounce1 by autoPathConfigPref("bounce1")
        val bounce2 by autoPathConfigPref("bounce2", defaultReverse = true)
        val bounce3 by autoPathConfigPref("bounce3")
        val bounce4 by autoPathConfigPref("bounce4", defaultReverse = true)
    }

    object InterstellarAccuracy : Named by Named("InterstellarAccuracy", this) {
        val zone1 by pref(4300, Rpm)
        val zone2 by pref(6000, Rpm)
        val zone3 by pref(5950, Rpm)
        val zone4 by pref(6150, Rpm)
    }

    object PowerPort : Named by Named("PowerPort", this) {
        val shootSpeed by pref(6000, Rpm)
        val pathConfig by autoPathConfigPref("")
        val distance by pref(12, Foot)
        val shootDelay by pref(0.5, Second)
    }

    init {
        AutoNav
        InterstellarAccuracy
        PowerPort
    }
}