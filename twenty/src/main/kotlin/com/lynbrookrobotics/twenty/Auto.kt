package com.lynbrookrobotics.twenty

import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.preferences.*

object Auto : Named by Named("Auto") {
    val id by pref(0)

    val recordFile: String by pref("0")
    val recordReverse by pref(false)

    val defaultPathConfig by autoPathConfigPref("0")

    object AutoNav : Named by Named("AutoNav", this) {
        val barrel by autoPathConfigPref("barrel")
        val slalom by autoPathConfigPref("slalom")

        // Bounce path files are split into bounce{1,2,3,4}.tsv
        val bounce by autoPathConfigPref("bounce")
    }

    init {
        AutoNav
    }
}