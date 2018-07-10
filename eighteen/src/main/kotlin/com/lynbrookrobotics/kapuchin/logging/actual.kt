package com.lynbrookrobotics.kapuchin.logging

actual fun printAtLevel(level: Level, formattedMessage: String) = when (level) {
    Level.Error -> println("ERROR $formattedMessage")
    Level.Warning -> println("WARNING $formattedMessage")
    Level.Debug -> println("DEBUG $formattedMessage")
}

actual typealias StackTraceElement = java.lang.StackTraceElement

actual val Throwable.platformStackTrace: Array<StackTraceElement> get() = stackTrace

actual fun nameLayer(parent: Named?, child: String): String =
        "${parent?.name?.plus('/') ?: ""}$child"