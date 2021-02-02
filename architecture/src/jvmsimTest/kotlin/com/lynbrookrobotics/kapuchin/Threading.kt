package com.lynbrookrobotics.kapuchin

import com.lynbrookrobotics.kapuchin.logging.*

fun threadDumpOnFailure(f: () -> Unit) {
    Named("Thread Dumper").apply {
        try {
            f()
        } catch (t: Throwable) {
            Thread.getAllStackTraces().forEach { (t, u) ->
                log(Level.Error, u) { t.name }
            }
            throw t
        }
    }
}