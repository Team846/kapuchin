package com.lynbrookrobotics.kapuchin.subsystems

import com.lynbrookrobotics.kapuchin.logging.Named
import com.lynbrookrobotics.kapuchin.timing.ExecutionOrder.Last
import com.lynbrookrobotics.kapuchin.timing.ticker
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
}