package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.logging.Named
import com.lynbrookrobotics.kapuchin.subsystems.Component
import com.lynbrookrobotics.kapuchin.subsystems.SubsystemHardware
import com.lynbrookrobotics.kapuchin.timing.Cancel
import com.lynbrookrobotics.kapuchin.timing.EventLoop
import com.lynbrookrobotics.kapuchin.timing.scope
import info.kunalsheth.units.generated.Second
import info.kunalsheth.units.generated.Time
import info.kunalsheth.units.generated.milli
import kotlinx.coroutines.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class Routine<C, H, Output> internal constructor(
        parent: C, name: String,
        private val controller: C.(Time) -> Output?,
        cont: CancellableContinuation<Unit>
) :
        CancellableContinuation<Unit> by cont,
        Named by Named(name, parent),
        (C, Time) -> Output

        where C : Component<C, H, Output>,
              H : SubsystemHardware<H, C> {

    override fun invoke(c: C, t: Time) =
            try {
                controller(c, t) ?: c.fallbackController(c, t).also { resume(Unit) }
            } catch (e: Throwable) {
                resumeWithException(e)
                c.fallbackController(c, t)
            }

    companion object {
        fun launchAll(vararg routines: suspend () -> Unit) = scope.launch {
            routines.forEach { launch { it() } }
        }

        infix fun (() -> Job).runWhile(predicate: () -> Boolean) = if (predicate()) {
            val job = this()
            var runOnTick: Cancel? = null

            runOnTick = EventLoop.runOnTick {
                if (!predicate()) {
                    runOnTick?.cancel()
                    job.cancel()
                }
            }

            job
        } else null

        suspend fun delay(time: Time) =
                delay(time.milli(Second).toLong())

        suspend fun withTimeout(time: Time, block: suspend () -> Unit) =
                withTimeoutOrNull(time.milli(Second).toLong()) { block() }
    }
}