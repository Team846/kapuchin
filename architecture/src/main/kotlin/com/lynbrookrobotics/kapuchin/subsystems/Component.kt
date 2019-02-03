package com.lynbrookrobotics.kapuchin.subsystems

import com.lynbrookrobotics.kapuchin.logging.Level.Debug
import com.lynbrookrobotics.kapuchin.logging.Level.Error
import com.lynbrookrobotics.kapuchin.logging.Named
import com.lynbrookrobotics.kapuchin.logging.log
import com.lynbrookrobotics.kapuchin.preferences.pref
import com.lynbrookrobotics.kapuchin.routines.Routine
import com.lynbrookrobotics.kapuchin.timing.blockingMutex
import com.lynbrookrobotics.kapuchin.timing.clock.Clock
import com.lynbrookrobotics.kapuchin.timing.clock.Clock.ExecutionOrder.Last
import com.lynbrookrobotics.kapuchin.timing.clock.Ticker
import com.lynbrookrobotics.kapuchin.timing.clock.Ticker.Companion.ticker
import info.kunalsheth.units.generated.Time
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
            setup: SensorScope.() -> This.(Time) -> Output?
    ) {
        val scope = SensorScope(this)
        try {
            val controller = scope.run(setup)
            suspendCancellableCoroutine<Unit> { cont ->
                routine = Routine(thisAsThis, name, controller, cont)
            }
            log(Debug) { "Completed $name routine." }
        } catch (c: CancellationException) {
            log(Debug) { "Cancelled $name routine.\n${c.message}" }
            throw c
        } catch (t: Throwable) {
            log(Error, t) { "Exception running $name routine.\n${t.message}" }
        } finally {
            scope.close()
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
        @Suppress("LeakingThis")
        clock.runOnTick(Last) { tickStart ->
            try {
                @Suppress("UNNECESSARY_SAFE_CALL")
                (routine ?: fallbackController)
                        ?.invoke(thisAsThis, tickStart)
                        ?.let { hardware.output(it) }
            } catch (t: Throwable) {
                routine?.resumeWithException(t) ?: log(Error, t) {
                    "Exception running default controller\n${t.message}"
                }
            }
        }
    }

    override fun equals(other: Any?) = when (other) {
        is Component<*, *, *> -> this.name == other.name
        else -> false
    }

    override fun hashCode() = name.hashCode()
}