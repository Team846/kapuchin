package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import info.kunalsheth.units.generated.*
import kotlinx.coroutines.CancellableContinuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


/**
 * Runs a routine by calculating the next output for the component every invoke.
 *
 * @author Kunal
 * @see Routine
 *
 * @param C type of this subsystem's component
 * @param H type of this subsystem's hardware
 * @param Output type of this subsystem's output
 */
internal class RoutineRunner<C, H, Output> internal constructor(
        private val routine: Routine<C, H, Output>,
        cont: CancellableContinuation<Unit>
) :
        CancellableContinuation<Unit> by cont,
        Named by Named(routine.name, routine.component),
        (C, Time) -> Output

        where C : Component<C, H, Output>,
              H : SubsystemHardware<H, C> {

    /**
     * Calculate the next subsystem output and manage this routine's lifecycle
     *
     * @param c this subsystem's component
     * @param t control loop start time
     * @return next subsystem output
     */
    override fun invoke(c: C, t: Time) =
            try {
                // Resume continuation if controller returns null
                routine.controller(c, t) ?: c.fallbackController(c, t).also { resume(Unit) }
            } catch (e: Throwable) {
                // Resume continuation with exception if controls throws error
                resumeWithException(e)
                c.fallbackController(c, t)
            }
}