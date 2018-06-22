package com.lynbrookrobotics.kapuchin.timing

import com.lynbrookrobotics.kapuchin.logging.Named

fun Named.PlatformThread(name: String, priority: Priority, run: () -> Unit) = PlatformThread(this, name, priority, run)

expect class PlatformThread(parent: Named, name: String, priority: Priority, run: () -> Unit)