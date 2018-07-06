package com.lynbrookrobotics.kapuchin.logging

actual fun Named.log(level: Level, throwable: Throwable, message: () -> String) = log(level, throwable.stackTrace, message)

actual fun printAtLevel(level: Level, formattedMessage: String) = when (level) {
    Level.Error -> println("ERROR $formattedMessage")
    Level.Warning -> println("WARNING $formattedMessage")
    Level.Debug -> println("DEBUG $formattedMessage")
}

actual typealias StackTraceElement = java.lang.StackTraceElement

actual fun nameLayer(parent: Named?, child: String): String =
        "${parent?.name?.plus('/') ?: ""}$child"