package com.lynbrookrobotics.kapuchin.timing

import com.lynbrookrobotics.kapuchin.logging.Named
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlin.concurrent.thread

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

actual val scope: CoroutineScope = GlobalScope