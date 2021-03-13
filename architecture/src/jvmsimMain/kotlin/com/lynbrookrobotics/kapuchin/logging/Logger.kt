@file:JvmName("Logger") // weird workaround

package com.lynbrookrobotics.kapuchin.logging

actual typealias StackTraceElement = java.lang.StackTraceElement

actual val Throwable.platformStackTrace: Array<StackTraceElement> get() = stackTrace
actual val stackTraceLimit = 100

internal actual class EventPrinter actual constructor(name: String) {
    actual fun printAtLevel(level: LogLevel, message: String) {
        println(message)
    }

    actual fun printDirect(message: String) {
        println(message)
    }
}