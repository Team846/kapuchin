package com.lynbrookrobotics.kapuchin.logging

import com.lynbrookrobotics.kapuchin.timing.coroutine
import info.kunalsheth.units.generated.Quan
import kotlinx.coroutines.launch
import kotlin.math.pow
import kotlin.math.round

fun Named.log(level: Level, throwable: Throwable, message: () -> String) = log(level, throwable.platformStackTrace, message)

fun Named.log(level: Level, stackTrace: Array<StackTraceElement>? = null, message: () -> String) = coroutine.launch {
    printAtLevel(level, messageToString(this@log, stackTrace, message))
}

private fun messageToString(sender: Named, stackTrace: Array<StackTraceElement>?, message: () -> String): String {
    val senderHeader = "${sender.name}: "
    val indent = " ".repeat(senderHeader.length)
    val newLine = "\n$indent"
    val indentedMessage = message().replace("\n", newLine)
    return "$senderHeader$indentedMessage" +
            (stackTrace?.joinToString(prefix = newLine, postfix = newLine, separator = newLine, limit = 7) ?: "")
}

expect fun printAtLevel(level: Level, formattedMessage: String)

infix fun Number.withDecimals(decimalPlaces: Int) = toDouble().let {
    val shifter = 10.0.pow(decimalPlaces)
    round(it * shifter) / shifter
}

infix fun <Q : Quan<Q>> Q.withDecimals(decimalPlaces: Int) = new(siValue withDecimals decimalPlaces)