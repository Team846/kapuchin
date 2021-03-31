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
        private val bounce1 by autoPathConfigPref("bounce1")
        private val bounce2 by autoPathConfigPref("bounce2", defaultReverse = true)
        private val bounce3 by autoPathConfigPref("bounce3")
        private val bounce4 by autoPathConfigPref("bounce4", defaultReverse = true)

        val bounce = arrayOf(bounce1, bounce2, bounce3, bounce4)
    }

    object GalacticSearch : Named by Named("Galactic Search", this) {
        val default by autoPathConfigPref("")
    }

    init {
        AutoNav
        GalacticSearch
    }
}