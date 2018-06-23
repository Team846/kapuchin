package com.lynbrookrobotics.kapuchin.logging

import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch

expect fun Named.log(level: Level, throwable: Throwable, message: suspend () -> String): Job

fun Named.log(level: Level, stackTrace: Array<StackTraceElement>? = emptyArray(), message: suspend () -> String) = launch {
    printAtLevel(level, messageToString(this@log, stackTrace, message))
}

private suspend fun messageToString(sender: Named, stackTrace: Array<StackTraceElement>?, message: suspend () -> String): String {
    val senderHeader = "${sender.name}: "
    val indent = " ".repeat(senderHeader.length)
    val newLine = "\n$indent"
    val indentedMessage = message().replace("\n", newLine)
    return "$senderHeader$indentedMessage$newLine" +
            (stackTrace?.joinToString(prefix = newLine, postfix = newLine, limit = 7) ?: "")
}

expect suspend fun printAtLevel(level: Level, formattedMessage: String)