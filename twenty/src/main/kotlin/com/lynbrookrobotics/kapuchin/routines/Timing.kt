package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.timing.*
import info.kunalsheth.units.generated.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.isActive

suspend fun CoroutineScope.delayUntil(
        poll: Time = 0.1.Second,
        f: () -> Boolean
) {
    while (isActive)
        if (f()) return
        else delay(poll)
}

fun Named.blockUntil(
        timeout: Time = 10.Second,
        poll: Time = 0.5.Second,
        f: () -> Boolean
): Boolean {
    if (!f()) {
        log(Debug) { "Waiting for predicated to return true..." }
        val startTime = currentTime
        while (!f() && currentTime - startTime < timeout) blockingDelay(poll)
    }
    return f()
}