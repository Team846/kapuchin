package com.lynbrookrobotics.kapuchin.timing

import com.lynbrookrobotics.kapuchin.logging.Named
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlin.concurrent.thread

actual class PlatformThread actual constructor(
        parent: Named,
        name: String,
        priority: Priority,
        run: () -> Unit
) {
    private val thread = thread(name = "${parent.name} $name Thread", block = run)
}

actual inline fun <R> blockingMutex(lock: Any, block: () -> R) = kotlin.synchronized(lock, block)

actual val coroutine: CoroutineScope = GlobalScope