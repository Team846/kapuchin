package com.lynbrookrobotics.kapuchin.logging

import com.lynbrookrobotics.kapuchin.timing.currentTime
import com.lynbrookrobotics.kapuchin.timing.scope
import info.kunalsheth.units.generated.Quan
import info.kunalsheth.units.generated.Second
import info.kunalsheth.units.generated.Time
import info.kunalsheth.units.generated.UomConverter
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import java.io.Closeable
import java.io.File
import java.io.Flushable

actual class Grapher<Q : Quan<Q>> private actual constructor(parent: Named, of: String, private val withUnits: UomConverter<Q>) :
        Named by Named("$of (${withUnits.unitName})", parent),
        Flushable, Closeable,
        (Time, Q) -> Unit {

    private val safeName = name.replace("""[^\w\d]""".toRegex(), "_")
    private val printer = File("/tmp/$safeName-${currentTime.Second}.csv")
            .printWriter(Charsets.US_ASCII).also { it.println("seconds,${withUnits.unitName}") }

    actual override fun invoke(x: Time, y: Q) {
        scope.launch(IO) { printer.println("${x.Second},${withUnits(y)}") }
    }

    actual override fun flush() = printer.flush()
    actual override fun close() = printer.close()

    actual companion object {
        actual fun <Q : Quan<Q>> Named.graph(of: String, withUnits: UomConverter<Q>) =
                Grapher(this, of, withUnits)
    }
}