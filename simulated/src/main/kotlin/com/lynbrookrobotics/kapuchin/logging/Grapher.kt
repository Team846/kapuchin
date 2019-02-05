package com.lynbrookrobotics.kapuchin.logging

import com.lynbrookrobotics.kapuchin.timing.*
import info.kunalsheth.units.generated.*
import kotlinx.coroutines.launch
import java.io.Closeable
import java.io.File
import java.io.Flushable

actual class Grapher<Q : Quan<Q>> internal actual constructor(parent: Named, of: String, private val withUnits: UomConverter<Q>) :
        Named by Named("$of (${withUnits.unitName})", parent),
        Flushable, Closeable,
        (Time, Q) -> Unit {

    private val safeName = name.replace("""[^\w\d]""".toRegex(), "_")
    private val printer = File("/tmp/$safeName-${currentTime.Second}.csv")
            .printWriter(Charsets.US_ASCII).also { it.println("seconds,${withUnits.unitName}") }

    actual override fun invoke(x: Time, y: Q) {
        scope.launch { printer.println("${x.Second},${withUnits(y)}") }
    }

    actual override fun flush() = printer.flush()
    actual override fun close() = printer.close()
}