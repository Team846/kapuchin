package com.lynbrookrobotics.kapuchin.timing

import com.lynbrookrobotics.kapuchin.subsystems.Named

expect class PlatformThread(parent: Named, name: String, priority: Priority, run: () -> Unit)