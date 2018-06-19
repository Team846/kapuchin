package com.lynbrookrobotics.kapuchin.logging

import kotlinx.coroutines.experimental.launch

fun Any.log(level: Level, stackTrace: Array<StackTraceElement>? = emptyArray(), message: suspend () -> String) = launch {
    printAtLevel(level, messageToString(this, stackTrace, message))
}

private suspend fun messageToString(sender: Any, stackTrace: Array<StackTraceElement>?, message: suspend () -> String): String {
    val senderHeader = "$sender: "
    val indent = " ".repeat(senderHeader.length)
    val newLine = "\n$indent"
    val indentedMessage = message().replace("\n", newLine)
    return "$senderHeader$indentedMessage$newLine" +
            (stackTrace?.joinToString(prefix = newLine, postfix = newLine, limit = 7) ?: "")
}

expect suspend fun printAtLevel(level: Level, formattedMessage: String)