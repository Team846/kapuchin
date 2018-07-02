package com.lynbrookrobotics.kapuchin.subsystems

import com.lynbrookrobotics.kapuchin.hardware.Sensor
import com.lynbrookrobotics.kapuchin.logging.Named
import com.lynbrookrobotics.kapuchin.timing.EventLoop
import com.lynbrookrobotics.kapuchin.timing.ExecutionOrder.Last
import com.lynbrookrobotics.kapuchin.timing.PlatformThread
import com.lynbrookrobotics.kapuchin.timing.Priority
import com.lynbrookrobotics.kapuchin.timing.Ticker.Companion.ticker
import info.kunalsheth.units.generated.Time

abstract class Component<This, H, Output>(val hardware: H) : Named(hardware.name, null)
        where This : Component<This, H, Output>,
              H : SubsystemHardware<H, This> {

    val ticker by lazy { ticker(hardware.priority, hardware.period) }

    var controller: (This.(Time) -> Output)? = null
    abstract val fallbackController: This.(Time) -> Output

    protected abstract fun H.output(value: Output)

    override fun equals(other: Any?) = when (other) {
        is Component<*, *, *> -> this.name == other.name
        else -> false
    }

    override fun hashCode() = name.hashCode()

    init {
        ticker.runOnTick(Last) { tickStart ->
            hardware.output(
                    @Suppress("UNCHECKED_CAST")
                    (controller ?: fallbackController)(this as This, tickStart)
            )
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
}