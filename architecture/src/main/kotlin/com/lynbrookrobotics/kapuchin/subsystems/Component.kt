package com.lynbrookrobotics.kapuchin.subsystems

import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import com.lynbrookrobotics.kapuchin.timing.clock.Clock.ExecutionOrder.*
import info.kunalsheth.units.generated.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resumeWithException

/**
 * Represents a robot subsystem's operations.
 *
 * Intended for writing autonomous routines, running control loops, and outputting to hardware.
 *
 * @author Kunal
 * @see SubsystemHardware
 * @see pref
 * @see Ticker
 *
 * @param This type of child class
 * @param H type of this subsystem's hardware
 * @param Output type of this subsystem's output
 *
 * @param hardware this subsystem's hardware
 */
abstract class Component<This, H, Output>(val hardware: H, customClock: Clock? = null) : Named by Named(hardware.name)
        where This : Component<This, H, Output>,
              H : SubsystemHardware<H, This> {

    @Suppress("UNCHECKED_CAST")
    private val thisAsThis = this as This

    /**
     * control loop's update source. If no `customClock` is specified, a `Ticker` is created.
     */
    val clock = customClock ?: ticker(hardware.priority, hardware.period)

    /**
     * controller to use when no `routine` is running.
     */
    abstract val fallbackController: This.(Time) -> Output

    /**
     * actively running routine
     */
    var routine: Routine<This, H, Output>? = null
        private set(value) = blockingMutex(this) {
            field.takeUnless { it === value }?.cancel()
            field = value
        }
        get() = field?.takeIf { it.isActive }

    /**
     * Setup and run a new routine
     *
     * After setup, the routine runs until it crashes, is cancelled, or its controller returns `null`.
     *
     * @param name logging name
     * @param setup function returning a subsystem controller
     */
    suspend fun startRoutine(
            name: String,
            setup: BoundSensorScope.() -> This.(Time) -> Output?
    ) {
        val sensorScope = BoundSensorScope(this)
        var routine: Routine<This, H, Output>? = null
        try {
            routine?.log(Debug) { "Starting" }
            val controller = sensorScope.run(setup)
            suspendCancellableCoroutine<Unit> { cont ->
                Routine(thisAsThis, name, controller, cont).also {
                    routine = it
                    this.routine = it
                }
            }
            routine?.log(Debug) { "Completed" }
        } catch (c: CancellationException) {
            routine?.log(Debug) { "Cancelled" }
            throw c
        } catch (t: Throwable) {
            routine?.log(Error, t)
        } finally {
            sensorScope.close()
        }
    }

    /**
     * Utility function to create a new subsystem controller
     *
     * @receiver this subsystem's component
     * @param Time loop start time
     * @return value to write to hardware or `null` to end the routine
     */
    fun controller(controller: This.(Time) -> Output?) = controller

    /**
     * Write the `value` to the hardware
     *
     * This function should be as low-level as possible. Besides safeties, there should never be any extra control code inside this function.
     *
     * @receiver this subsystem's hardware
     * @param value output to write
     */
    protected abstract fun H.output(value: Output)

    init {
        val subsystemBirth = currentTime
        clock.runOnTick(Last) { tickStart: Time ->
            if (tickStart - subsystemBirth > 1.Second) try {
                (routine ?: fallbackController)
                        .invoke(thisAsThis, tickStart)
                        .let { hardware.output(it) }
            } catch (t: Throwable) {
                routine?.resumeWithException(t) ?: log(Error, t)
            }
        }
    }

    override fun equals(other: Any?) = when (other) {
        is Component<*, *, *> -> this.name == other.name
        else -> false
    }

    override fun hashCode() = name.hashCode()
}