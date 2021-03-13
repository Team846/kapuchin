package com.lynbrookrobotics.kapuchin

import com.lynbrookrobotics.kapuchin.logging.*

fun threadDumpOnFailure(f: () -> Unit) {
    Named("Thread Dumper").apply {
        try {
            f()
        } catch (t: Throwable) {
            Thread.getAllStackTraces().forEach { (t, u) ->
                logStackTrace(u) { t.name }
            }
            throw t
        }
    }
}