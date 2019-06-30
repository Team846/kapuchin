package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import info.kunalsheth.units.generated.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Create a new `Routine` (Doesn't run).
 *
 * @author Andy
 * @see Component
 *
 * @receiver subsystem component
 * @param name logging name
 * @param setup function returning a subsystem controller
 * @return a new `Routine`
 */
fun <C, H, Output> Component<C, H, Output>.newRoutine(
        name: String,
        setup: BoundSensorScope.() -> C.(Time) -> Output?
): Routine<C, H, Output>
        where C : Component<C, H, Output>,
              H : SubsystemHardware<H, C> {

    val sensorScope = BoundSensorScope(this)
    return Routine(thisAsThis, name, sensorScope.run(setup), sensorScope)

}

/**
 * Utility function to create a new subsystem controller
 *
 * @receiver this subsystem's component
 * @return value to write to hardware or `null` to end the routine
 */
fun <C, H, Output> Component<C, H, Output>.controller(
        controller: C.(Time) -> Output?
)
        where C : Component<C, H, Output>,
              H : SubsystemHardware<H, C> = controller

/**
 * Represents an active subsystem routine.
 *
 * Routines run until their controller returns null, they throw an exception, or they are cancelled.
 *
 * @author Kunal
 * @see RoutineRunner
 * @see Component
 *
 * @param C type of this subsystem's component
 * @param H type of this subsystem's hardware
 * @param Output type of this subsystem's output
 */
class Routine<C, H, Output> internal constructor(
        private val component: C,
        private val name: String,
        private val controller: C.(Time) -> Output?,
        private val sensorScope: BoundSensorScope
)
        where C : Component<C, H, Output>,
              H : SubsystemHardware<H, C> {

    /**
     * Start the routine by creating a new `routineRunner` for the subsystem.
     */
    suspend fun start() {
        var runner: RoutineRunner<C, H, Output>? = null
        try {
            runner?.log(Debug) { "Starting" }
            suspendCancellableCoroutine<Unit> { cont ->
                RoutineRunner(component, name, controller, cont).also {
                    runner = it
                    component.routineRunner = it
                }
            }
            runner?.log(Debug) { "Completed" }
        } catch (c: CancellationException) {
            runner?.log(Debug) { "Cancelled" }
            throw c
        } catch (t: Throwable) {
            runner?.log(Error, t)
        } finally {
            sensorScope.close()
        }
    }
}