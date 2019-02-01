package com.lynbrookrobotics.kapuchin.tests

import com.lynbrookrobotics.kapuchin.logging.Level
import com.lynbrookrobotics.kapuchin.logging.Named
import com.lynbrookrobotics.kapuchin.logging.log

fun threadDumpOnFailiure(f: () -> Unit) {
    Named("Thread Dumper").apply {
        try {
            f()
        } catch (t: Throwable) {
            Thread.getAllStackTraces().forEach { t, u ->
                log(Level.Error, u) { t.name }
            }
            throw t
        }
    }
}