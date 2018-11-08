package com.lynbrookrobotics.kapuchin.timing

import com.lynbrookrobotics.kapuchin.logging.Named
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

fun Named.platformThread(name: String, priority: Priority, run: () -> Unit) = PlatformThread(this, name, priority, run)

expect class PlatformThread(parent: Named, name: String, priority: Priority, run: () -> Unit)

expect inline fun <R> blockingMutex(lock: Any, block: () -> R): R

expect val coroutine: CoroutineScope