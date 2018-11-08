package com.lynbrookrobotics.kapuchin.logging

import com.lynbrookrobotics.kapuchin.timing.coroutine
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import info.kunalsheth.units.generated.Quan
import info.kunalsheth.units.generated.Second
import info.kunalsheth.units.generated.Time
import info.kunalsheth.units.generated.UomConverter
import kotlinx.coroutines.launch
import java.io.File
import kotlin.text.Charsets.US_ASCII

actual fun printAtLevel(level: Level, formattedMessage: String) = when (level) {
    Level.Error -> println("ERROR $formattedMessage")
    Level.Warning -> println("WARNING $formattedMessage")
    Level.Debug -> println("DEBUG $formattedMessage")
}

actual class Grapher<Q : Quan<Q>> private actual constructor(parent: Named, of: String, private val withUnits: UomConverter<Q>) :
        Named by Named("$of (${withUnits.unitName})", parent),
        (Time, Q) -> Unit {

    private var running = coroutine.launch { }
    private val safeName = name.replace("""[^\w\d]""".toRegex(), "_")
    private val printer = File("/tmp/$safeName.csv")
            .printWriter(US_ASCII).also { it.println("value,stamp") }

    actual override fun invoke(stamp: Time, value: Q) {
        if (running.isCompleted) coroutine.launch {
            SmartDashboard.putNumber(name, withUnits(value))
            printer.println("$value,${stamp.Second}")
        }.also { running = it }
    }

    actual companion object {
        actual fun <Q : Quan<Q>> Named.graph(of: String, withUnits: UomConverter<Q>) =
                Grapher(this, of, withUnits)
    }
}

actual typealias StackTraceElement = java.lang.StackTraceElement

actual val Throwable.platformStackTrace: Array<StackTraceElement> get() = stackTrace

actual fun nameLayer(parent: Named?, child: String): String = "${parent?.name?.plus('/') ?: ""}$child"