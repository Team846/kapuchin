package com.lynbrookrobotics.kapuchin.subsystems

import com.lynbrookrobotics.kapuchin.hardware.Sensor
import com.lynbrookrobotics.kapuchin.logging.Named
import com.lynbrookrobotics.kapuchin.routines.Routine
import com.lynbrookrobotics.kapuchin.timing.Clock
import com.lynbrookrobotics.kapuchin.timing.EventLoop
import com.lynbrookrobotics.kapuchin.timing.ExecutionOrder.Last
import com.lynbrookrobotics.kapuchin.timing.Ticker.Companion.ticker
import com.lynbrookrobotics.kapuchin.timing.currentTime
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

    suspend fun runRoutine(name: String, controller: This.(Time) -> Output?) {
        try {
            suspendCancellableCoroutine<Unit> { cont ->
                routine = Routine(thisAsThis, name, controller, cont)
            }
        } catch (e: CancellationException) {
        }
    }

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

    val <Input> Sensor<Input>.readOnTick
        get() = Sensor.UpdateSource(this, startUpdates = { s ->
            clock.runOnTick { s.value = optimizedRead(it, hardware.syncThreshold) }
        })

    val <Input> Sensor<Input>.readWithEventLoop
        get() = Sensor.UpdateSource(this, startUpdates = { _ ->
            EventLoop.runOnTick { value = optimizedRead(it, hardware.syncThreshold) }
        })

    val <Input> Sensor<Input>.readEagerly
        get() = Sensor.UpdateSource(this, getValue = { _ ->
            optimizedRead(currentTime, hardware.syncThreshold).also { value = it }
        })

    override fun equals(other: Any?) = when (other) {
        is Component<*, *, *> -> this.name == other.name
        else -> false
    }

    override fun hashCode() = name.hashCode()
}