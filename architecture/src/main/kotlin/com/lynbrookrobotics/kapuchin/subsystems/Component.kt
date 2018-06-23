package com.lynbrookrobotics.kapuchin.subsystems

import com.lynbrookrobotics.kapuchin.logging.Named
import com.lynbrookrobotics.kapuchin.timing.ExecutionOrder.Last
import com.lynbrookrobotics.kapuchin.timing.Ticker
import info.kunalsheth.units.generated.Time

abstract class Component<This, H, Output>(val hardware: H) : Named(null, hardware.name)
        where This : Component<This, H, Output>,
              H : SubsystemHardware<H, This> {

    val ticker by lazy { Ticker(hardware.priority, hardware.period) }

    var controller: (This.(Time) -> Output)? = null
    abstract val fallbackController: This.(Time) -> Output

    protected abstract fun H.output(value: Output)

    init {
        ticker.runOnTick(Last) { tickStart ->
            hardware.output(
                    @Suppress("UNCHECKED_CAST")
                    (controller ?: fallbackController)(this as This, tickStart)
            )
        }
    }
}