package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.logging.Named
import com.lynbrookrobotics.kapuchin.subsystems.Component
import com.lynbrookrobotics.kapuchin.subsystems.SubsystemHardware
import com.lynbrookrobotics.kapuchin.timing.Cancel
import com.lynbrookrobotics.kapuchin.timing.EventLoop
import info.kunalsheth.units.generated.Time
import kotlinx.coroutines.experimental.CancellableContinuation
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch

class Routine<C, H, Output>(
        parent: C, name: String,
        private val controller: C.(Time) -> Output?,
        private val cont: CancellableContinuation<Unit>
) :
        Named(name, parent),
        (C, Time) -> Output,
        Job by cont as Job

        where C : Component<C, H, Output>,
              H : SubsystemHardware<H, C> {

    override fun invoke(c: C, t: Time) = controller(c, t)
            ?: c.fallbackController(c, t).also { cont.resume(Unit) }

    companion object {
        fun launchAll(vararg routines: suspend () -> Unit) = launch {
            routines.forEach { launch(coroutineContext) { it() } }
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
    }
}