package com.lynbrookrobotics.kapuchin.subsystems

import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import com.lynbrookrobotics.kapuchin.timing.clock.Clock.ExecutionOrder.*
import info.kunalsheth.units.generated.*
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

    @Suppress("UNCHECKED_CAST", "LeakingThis")
    internal val thisAsThis = this as This

    /**
     * Control loop's update source. If no `customClock` is specified, a `Ticker` is created.
     */
    val clock = customClock ?: ticker(hardware.priority, hardware.period)

    /**
     * Controller to use when no `routine` is running.
     */
    abstract val fallbackController: This.(Time) -> Output

    /**
     * Current `RoutineRunner`.
     */
    internal var routineRunner: RoutineRunner<This, H, Output>? = null
        internal set(value) = blockingMutex(this) {
            field.takeUnless { it === value }?.cancel()
            field = value
        }
        get() = field?.takeIf { it.isActive }



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
                (routineRunner ?: fallbackController)
                        ?.invoke(thisAsThis, tickStart)
                        ?.let { hardware?.output(it) }
            } catch (t: Throwable) {
                routineRunner?.resumeWithException(t) ?: log(Error, t)
            }
        }
    }

    override fun equals(other: Any?) = when (other) {
        is Component<*, *, *> -> this.name == other.name
        else -> false
    }

    override fun hashCode() = name.hashCode()
}