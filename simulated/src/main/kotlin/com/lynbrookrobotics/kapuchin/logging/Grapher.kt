package com.lynbrookrobotics.kapuchin.logging

import com.lynbrookrobotics.kapuchin.timing.currentTime
import info.kunalsheth.units.generated.Second
import info.kunalsheth.units.generated.Time
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import java.io.File
import java.nio.charset.StandardCharsets.US_ASCII

actual class Grapher actual constructor(parent: Named, name: String) : Named(name, parent), (Time, Double) -> Job {
    private val safeName = name.replace("""[^\w\d]""".toRegex(), "_")
    private val printer = File("/tmp/$safeName-${currentTime.Second}.csv")
            .printWriter(US_ASCII).also { it.println("value,stamp") }

    actual override fun invoke(stamp: Time, value: Double) = launch {
        printer.println("$value,${stamp.Second}")
    }
}