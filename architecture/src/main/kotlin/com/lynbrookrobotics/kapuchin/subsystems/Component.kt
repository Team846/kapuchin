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

    private fun <Input> Sensor<Input>.readOnTick() = startUpdates<This> { _, _ ->
        ticker.runOnTick { value = optimizedRead(it, hardware.syncThreshold) }
    }

    private fun <Input> Sensor<Input>.readWithEventLoop() = startUpdates<This> { _, _ ->
        EventLoop.runOnTick { value = optimizedRead(it, hardware.syncThreshold) }
    }

    private fun <Input> Sensor<Input>.readAsynchronously(name: String, priority: Priority) = startUpdates<This> { _, _ ->
        PlatformThread(this@Component, name, priority) {
            while (true) value = optimizedRead(ticker.waitOnTick(), hardware.syncThreshold)
        }
    }

    private fun <Input> Sensor<Input>.readEagerly() = startUpdates<This> { _, _ -> }
}