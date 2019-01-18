package com.lynbrookrobotics.kapuchin.timing

import com.lynbrookrobotics.kapuchin.logging.Level
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.logging.Named
import com.lynbrookrobotics.kapuchin.logging.log
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.concurrent.thread
import kotlin.coroutines.CoroutineContext

actual class PlatformThread actual private constructor(
        parent: Named,
        name: String,
        priority: Priority,
        run: () -> Unit
) {
    private val thread = thread(name = "${parent.name} $name Thread", block = run)

    actual companion object {
        actual fun Named.platformThread(name: String, priority: Priority, run: () -> Unit) = PlatformThread(this, name, priority, run)
    }
}

actual inline fun <R> blockingMutex(lock: Any, block: () -> R) = kotlin.synchronized(lock, block)

actual val scope = CoroutineScope(Dispatchers.IO +
        CoroutineName("Kapuchin Coroutine Scope") +
        CoroutineExceptionHandler { _, throwable: Throwable ->
            Named("Coroutines").log(Error, throwable) { "Exception thrown from coroutine" }
        }
)