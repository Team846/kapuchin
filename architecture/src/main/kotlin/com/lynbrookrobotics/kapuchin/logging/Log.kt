package com.lynbrookrobotics.kapuchin.logging

import info.kunalsheth.units.generated.Quan
import kotlinx.coroutines.Job
import kotlinx.coroutines.sync.Mutex
import kotlin.math.pow
import kotlin.math.round

internal val printMutex = Mutex()

/**
 * Logs an exception stacktrace
 *
 * @author Kunal
 *
 * @receiver owner of the exception
 * @param level message importance
 * @param throwable data to log
 * @param message any additional information
 * @return asynchronous logging job
 */
fun Named.log(level: Level, throwable: Throwable, message: () -> String) = log(level, throwable.platformStackTrace, message)

/**
 * Logs an exception stacktrace
 *
 * @author Kunal
 *
 * @receiver owner of the exception
 * @param level message importance
 * @param stackTrace data to log
 * @param message any additional information
 * @return asynchronous logging job
 */
fun Named.log(level: Level, stackTrace: Array<StackTraceElement>? = null, message: () -> String) =
        printAtLevel(level, messageToString(this@log, stackTrace, message))

private fun messageToString(sender: Named, stackTrace: Array<StackTraceElement>?, message: () -> String): String {
    val senderHeader = "${sender.name}: "
    val indent = " ".repeat(senderHeader.length)
    val newLine = "\n$indent"
    val indentedMessage = message().replace("\n", newLine)
    return "$senderHeader$indentedMessage" +
            (stackTrace?.joinToString(prefix = newLine, postfix = newLine, separator = newLine, limit = 7) ?: "")
}

/**
 * Writes a message to the log
 *
 * @author Kunal
 *
 * @param level message importance
 * @param formattedMessage data to log
 */
expect fun printAtLevel(level: Level, formattedMessage: String): Job

/**
 * Rounds a number to a fixed number of decimal places for cleaner logging
 *
 * @author Kunal
 *
 * @receiver number to round
 * @param decimalPlaces number of digits to keep left of the decimal. Negative numbers round to the right of the decimal.
 * @return asynchronous logging job
 */
infix fun Number.withDecimals(decimalPlaces: Int) = toDouble().let {
    val shifter = 10.0.pow(decimalPlaces)
    round(it * shifter) / shifter
}

/**
 * Rounds a number to a fixed number of decimal places for cleaner logging
 *
 * @author Kunal
 *
 * @receiver number to round
 * @param decimalPlaces number of digits to keep left of the decimal. Negative numbers round to the right of the decimal.
 * @return asynchronous logging job
 */
infix fun <Q : Quan<Q>> Q.withDecimals(decimalPlaces: Int) = new(siValue withDecimals decimalPlaces)