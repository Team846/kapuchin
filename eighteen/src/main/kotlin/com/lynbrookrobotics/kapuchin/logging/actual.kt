package com.lynbrookrobotics.kapuchin.logging

import edu.wpi.first.wpilibj.DriverStation

actual fun Named.log(level: Level, throwable: Throwable, message: suspend () -> String) = log(level, throwable.stackTrace, message)

actual suspend fun printAtLevel(level: Level, formattedMessage: String) = when (level) {
    Level.Error -> DriverStation.reportError(formattedMessage, false)
    Level.Warning -> DriverStation.reportWarning(formattedMessage, false)
    Level.Debug -> println(formattedMessage)
}

actual typealias StackTraceElement = java.lang.StackTraceElement

actual fun nameLayer(parent: Named?, child: String): String = "${parent?.name ?: ""}/$child"