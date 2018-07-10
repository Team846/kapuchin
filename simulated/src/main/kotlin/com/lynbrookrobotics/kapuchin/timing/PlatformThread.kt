package com.lynbrookrobotics.kapuchin.timing

import com.lynbrookrobotics.kapuchin.logging.Named
import kotlin.concurrent.thread

actual class PlatformThread actual constructor(
        parent: Named,
        name: String,
        priority: Priority,
        run: () -> Unit
) {
    private val thread = thread(name = "${parent.name} $name Thread", block = run)
}