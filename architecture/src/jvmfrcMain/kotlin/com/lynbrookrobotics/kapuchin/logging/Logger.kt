@file:JvmName("Logger") // weird workaround

package com.lynbrookrobotics.kapuchin.logging

import com.lynbrookrobotics.kapuchin.logging.LogLevel.*
import edu.wpi.first.wpilibj.DriverStation
import java.io.File
import java.io.PrintWriter

actual typealias StackTraceElement = java.lang.StackTraceElement

actual val Throwable.platformStackTrace: Array<StackTraceElement> get() = stackTrace
actual val stackTraceLimit = 5

internal actual class EventPrinter actual constructor(name: String) {

    private val writer = File("/home/lvuser/$name.log")
        .also { it.createNewFile() }
        .bufferedWriter()
        .let { PrintWriter(it, true) } // enable auto flush after each line

    actual fun printAtLevel(level: LogLevel, message: String) {
        writer.println(message)

        when (level) {
            ERROR -> DriverStation.reportError(message, false)
            WARN -> DriverStation.reportWarning(message, false)
            else -> Unit
        }
    }

    actual fun printDirect(message: String) {
        writer.println(message)
    }
}
