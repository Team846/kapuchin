package com.lynbrookrobotics.kapuchin.subsystems

import com.lynbrookrobotics.kapuchin.hardware.Sensor
import com.lynbrookrobotics.kapuchin.logging.Named
import com.lynbrookrobotics.kapuchin.routines.Routine
import com.lynbrookrobotics.kapuchin.timing.EventLoop
import com.lynbrookrobotics.kapuchin.timing.ExecutionOrder.Last
import com.lynbrookrobotics.kapuchin.timing.PlatformThread
import com.lynbrookrobotics.kapuchin.timing.Priority
import com.lynbrookrobotics.kapuchin.timing.Ticker.Companion.ticker
import info.kunalsheth.units.generated.Time
import kotlinx.coroutines.experimental.suspendCancellableCoroutine

abstract class Component<This, H, Output>(val hardware: H) : Named(hardware.name, null)
        where This : Component<This, H, Output>,
              H : SubsystemHardware<H, This> {

    @Suppress("UNCHECKED_CAST")
    private val thisAsThis = this as This

    val ticker = ticker(hardware.priority, hardware.period)

    abstract val fallbackController: This.(Time) -> Output

    var routine: Routine<This, H, Output>? = null
        private set
        get() = field?.takeIf { it.isActive }

    suspend fun runRoutine(name: String, controller: This.(Time) -> Output?) = suspendCancellableCoroutine<Unit> { cont ->
        routine = Routine(thisAsThis, name, controller, cont)
    }

    protected abstract fun H.output(value: Output)

    init {
        ticker.runOnTick(Last) { tickStart ->
            @Suppress("UNNECESSARY_SAFE_CALL")
            (routine ?: fallbackController)
                    ?.invoke(thisAsThis, tickStart)
                    ?.let { hardware.output(it) }
        }
    }

    val <Input> Sensor<Input>.readOnTick
        get() = startUpdates { _ ->
            ticker.runOnTick { value = optimizedRead(it, hardware.syncThreshold) }
        }

    val <Input> Sensor<Input>.readWithEventLoop
        get() = startUpdates { _ ->
            EventLoop.runOnTick { value = optimizedRead(it, hardware.syncThreshold) }
        }

    fun <Input> Sensor<Input>.readAsynchronously(priority: Priority) = startUpdates {
        PlatformThread(this@Component, it.name, priority) {
            while (true) value = optimizedRead(ticker.waitOnTick(), hardware.syncThreshold)
        }
    }

    val <Input> Sensor<Input>.readEagerly get() = startUpdates { _ -> }

    override fun equals(other: Any?) = when (other) {
        is Component<*, *, *> -> this.name == other.name
        else -> false
    }

    override fun hashCode() = name.hashCode()
}