package com.lynbrookrobotics.kapuchin.logging

import com.lynbrookrobotics.kapuchin.timing.scope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withLock

actual fun printAtLevel(level: Level, formattedMessage: String) = scope.launch(IO) {
    printMutex.withLock {
        when (level) {
            Level.Error -> println("ERROR $formattedMessage")
            Level.Warning -> println("WARNING $formattedMessage")
            Level.Debug -> println("DEBUG $formattedMessage")
        }
    }
}