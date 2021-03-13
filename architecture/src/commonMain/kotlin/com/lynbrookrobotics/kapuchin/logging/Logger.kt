package com.lynbrookrobotics.kapuchin.logging

import com.lynbrookrobotics.kapuchin.logging.LogLevel.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.timing.*
import info.kunalsheth.units.generated.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.pow
import kotlin.math.round

expect class StackTraceElement

expect val Throwable.platformStackTrace: Array<StackTraceElement>
expect val stackTraceLimit: Int

/**
 * Log message severity.
 *
 * - ERROR: a fatal error that requires immediate attention. e.g. failed subsystem initialization.
 * - WARN: a potential failure point or edge case behavior. e.g. loop overruns, limelight not detecting target.
 * - INFO: generally useful information (the "default" level). e.g. choreography started/ended/cancelled.
 * - DEBUG: diagnostic information that may be verbose. e.g. waypoint hit during trajectory following.
 *
 * @author Andy
 */
enum class LogLevel {
    ERROR, WARN, INFO, DEBUG
}

/**
 * The base logger class that logs messages at a platform-specific level.
 *
 * @param name the name of the logger.
 * @author Andy
 */
internal expect class EventPrinter(name: String) {
    fun printAtLevel(level: LogLevel, message: String)
    fun printDirect(message: String)
}

/**
 * An event logger that
 */
class EventLogger(name: String) {

    private val channel = Channel<Pair<LogLevel?, String>>(UNLIMITED)
    private val printer = EventPrinter(name)

    init {
        allLoggers.add(this)
        scope.launch {
            while (isActive) {
                val (level, message) = channel.receive()
                if (level != null)
                    printer.printAtLevel(level, message)
                else
                    printer.printDirect(message)
            }
        }
    }

    /**
     * Format the message with the [Named] sender and the [LogLevel] and send to the message channel.
     */
    internal suspend fun log(sender: Named, level: LogLevel, stackTrace: Array<StackTraceElement>?, message: String) {
        val ansiColor = if (colorOutput) when (level) {
            ERROR -> "\u001B[31m" // red
            WARN -> "\u001B[33m"  // yellow
            INFO -> "\u001B[32m"  // green
            DEBUG -> "\u001B[0m"  // no color (reset)
        } else ""
        val ansiReset = if (colorOutput) "\u001B[0m" else ""

        val senderHeader = "$ansiColor[${level.name} ${sender.name}]$ansiReset "
//        val indent = " ".repeat(senderHeader.length - ansiColor.length - ansiReset.length)
        val newLine = "\n\t"
        val indentedMessage = message.replace("\n", newLine)
        val stackTraceMessage = stackTrace?.joinToString(
            prefix = newLine,
            postfix = newLine,
            separator = newLine,
            limit = stackTraceLimit
        ) ?: ""

        channel.send(level to "$senderHeader$indentedMessage$stackTraceMessage")
    }

    /**
     * Add a direct message to the message channel.
     */
    internal suspend fun direct(message: String) {
        channel.send(null to message)
    }

    companion object : Named by Named("Event Logger") {
        /**
         * Enable or disable ANSI console color output
         */
//        private val colorOutput by pref(true)
        private val colorOutput = true

        /**
         * A list of all initialized loggers.
         */
        private val allLoggers = mutableListOf<EventLogger>()

        /**
         * Flush all initialized loggers by waiting for their message channel to be empty.
         */
        suspend fun flushAll() = coroutineScope {
            allLoggers.forEach {
                launch {
                    delayUntil { it.channel.isEmpty }
                }
            }
        }
    }
}

/**
 * Default global logger
 */
val defaultRobotLogger = EventLogger("robot")

/**
 * Log a message and a [LogLevel] to an [EventLogger].
 */
fun Named.log(logger: EventLogger, level: LogLevel, message: () -> String) = scope.launch {
    logger.log(this@log, level, null, message())
}

/**
 * Log a message and a [LogLevel] to the [defaultRobotLogger].
 */
fun Named.log(level: LogLevel, message: () -> String) = log(defaultRobotLogger, level, message)

/**
 * Log a [Throwable] to an [EventLogger] as an [ERROR].
 */
fun Named.log(logger: EventLogger, level: LogLevel, throwable: Throwable) = scope.launch {
    logger.log(
        this@log, level, null, """
        $throwable
        Message: ${throwable.message}
        Cause:   ${throwable.cause}
        """.trimIndent()
    )
}

/**
 * Log a [Throwable] to the [defaultRobotLogger] as an [ERROR].
 */
fun Named.log(level: LogLevel, throwable: Throwable) = log(defaultRobotLogger, level, throwable)

/**
 * Log a [Throwable] as a stack trace and a message to an [EventLogger] as an [ERROR].
 */
fun Named.logStackTrace(logger: EventLogger, throwable: Throwable, message: () -> String): Job = scope.launch {
    logger.log(this@logStackTrace, ERROR, throwable.platformStackTrace, message())
}

/**
 * Log a [Throwable] as a stack trace and a message to the [defaultRobotLogger] as an [ERROR].
 */
fun Named.logStackTrace(throwable: Throwable, message: () -> String) =
    logStackTrace(defaultRobotLogger, throwable, message)

/**
 * Log a raw stack trace and a message to an [EventLogger] as an [ERROR].
 */
fun Named.logStackTrace(logger: EventLogger, stackTrace: Array<StackTraceElement>, message: () -> String) =
    scope.launch {
        logger.log(this@logStackTrace, ERROR, stackTrace, message())
    }

/**
 * Log a raw stack trace and a message to the [defaultRobotLogger] as an [ERROR].
 */
fun Named.logStackTrace(stackTrace: Array<StackTraceElement>, message: () -> String) =
    logStackTrace(defaultRobotLogger, stackTrace, message)

/**
 * Log a direct message without log formatting to an [EventLogger].
 */
fun Named.logDirect(logger: EventLogger, message: () -> String) = scope.launch {
    logger.direct(message())
}

/**
 * Log a direct message without formatting to the [defaultRobotLogger]
 */
fun Named.logDirect(message: () -> String) = logDirect(defaultRobotLogger, message)

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