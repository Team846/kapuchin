package com.lynbrookrobotics.kapuchin.subsystems

import com.lynbrookrobotics.kapuchin.logging.Level.Debug
import com.lynbrookrobotics.kapuchin.logging.Level.Error
import com.lynbrookrobotics.kapuchin.logging.Named
import com.lynbrookrobotics.kapuchin.logging.log
import com.lynbrookrobotics.kapuchin.logging.platformStackTrace
import com.lynbrookrobotics.kapuchin.routines.Routine
import com.lynbrookrobotics.kapuchin.timing.Clock
import com.lynbrookrobotics.kapuchin.timing.ExecutionOrder.Last
import com.lynbrookrobotics.kapuchin.timing.Ticker.Companion.ticker
import info.kunalsheth.units.generated.Time
import kotlinx.coroutines.experimental.CancellationException
import kotlinx.coroutines.experimental.suspendCancellableCoroutine

abstract class Component<This, H, Output>(val hardware: H, customClock: Clock? = null) : Named(hardware.name, null)
        where This : Component<This, H, Output>,
              H : SubsystemHardware<H, This> {

    @Suppress("UNCHECKED_CAST")
    private val thisAsThis = this as This

    val clock = customClock ?: ticker(hardware.priority, hardware.period)

    abstract val fallbackController: This.(Time) -> Output

    var routine: Routine<This, H, Output>? = null
        private set(value) = synchronized(this) {
            field.takeUnless { it === value }?.cancel()
            field = value
        }
        get() = synchronized(this) { field?.takeIf { it.isActive } }

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
        } catch (c: CancellationException) {
            log(Debug) { "${hardware.subsystemName}'s $name routine was cancelled.\nMessage: ${c.message}" }
        } catch (t: Throwable) {
            log(Error, t.platformStackTrace) { "An exception was thrown from ${hardware.subsystemName}'s $name routine.\nMessage: ${t.message}" }
        } finally {
            scope.close()
        }
    }

    fun controller(controller: This.(Time) -> Output?) = controller

    protected abstract fun H.output(value: Output)

    init {
        @Suppress("LeakingThis")
        clock.runOnTick(Last) { tickStart ->
            @Suppress("UNNECESSARY_SAFE_CALL")
            (routine ?: fallbackController)
                    ?.invoke(thisAsThis, tickStart)
                    ?.let { hardware.output(it) }
        }
    }

    override fun equals(other: Any?) = when (other) {
        is Component<*, *, *> -> this.name == other.name
        else -> false
    }

    override fun hashCode() = name.hashCode()
}