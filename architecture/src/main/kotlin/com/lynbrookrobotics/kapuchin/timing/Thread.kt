package com.lynbrookrobotics.kapuchin.timing

import com.lynbrookrobotics.kapuchin.logging.Named
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope

fun Named.platformThread(name: String, priority: Priority, run: () -> Unit) = PlatformThread(this, name, priority, run)

expect class PlatformThread(parent: Named, name: String, priority: Priority, run: () -> Unit)

expect val coroutine: CoroutineScope