package com.lynbrookrobotics.kapuchin.timing

expect class PlatformThread(name: String, priority: Priority, run: () -> Unit)