package com.lynbrookrobotics.kapuchin.logging

import info.kunalsheth.units.generated.Quan
import com.lynbrookrobotics.kapuchin.timing.currentTime
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.generated.Second
import info.kunalsheth.units.generated.Time
import info.kunalsheth.units.generated.UomConverter
import kotlinx.coroutines.experimental.launch
import java.io.File
import kotlin.text.Charsets.US_ASCII

actual fun printAtLevel(level: Level, formattedMessage: String) = when (level) {
    Level.Error -> println("ERROR $formattedMessage")
    Level.Warning -> println("WARNING $formattedMessage")
    Level.Debug -> println("DEBUG $formattedMessage")
}

actual class Grapher<Q : Quan<Q>> private actual constructor(parent: Named, of: String, private val withUnits: UomConverter<Q>) :
        Named("$of (${withUnits.unitName})", parent),
        (Time, Q) -> Unit {

    private var running = launch { }
    private val safeName = name.replace("""[^\w\d]""".toRegex(), "_")
    private val printer = File("/tmp/$safeName.csv")
            .printWriter(US_ASCII).also { it.println("${Second.unitName},${withUnits.unitName}") }

    actual override fun invoke(stamp: Time, value: Q) {
        if (running.isCompleted) launch {
            val converted = withUnits(value)
//            SmartDashboard.putNumber(name, converted)
            printer.println("${stamp.Second},$converted")
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