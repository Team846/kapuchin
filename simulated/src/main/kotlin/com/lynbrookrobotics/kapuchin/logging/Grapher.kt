package com.lynbrookrobotics.kapuchin.logging

import com.lynbrookrobotics.kapuchin.timing.coroutine
import com.lynbrookrobotics.kapuchin.timing.currentTime
import info.kunalsheth.units.generated.Quan
import info.kunalsheth.units.generated.Second
import info.kunalsheth.units.generated.Time
import info.kunalsheth.units.generated.UomConverter
import kotlinx.coroutines.launch
import java.io.File

actual class Grapher<Q : Quan<Q>> private actual constructor(parent: Named, of: String, private val withUnits: UomConverter<Q>) :
        Named("$of (${withUnits.unitName})", parent),
        (Time, Q) -> Unit {

    private val safeName = name.replace("""[^\w\d]""".toRegex(), "_")
    private val printer = File("/tmp/$safeName-${currentTime.Second}.csv")
            .printWriter(Charsets.US_ASCII).also { it.println("value,stamp") }

    actual override fun invoke(stamp: Time, value: Q) {
        coroutine.launch { printer.println("$value,${stamp.Second}") }
    }

    actual companion object {
        actual fun <Q : Quan<Q>> Named.graph(of: String, withUnits: UomConverter<Q>) =
                Grapher(this, of, withUnits)
    }
}