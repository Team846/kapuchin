package com.lynbrookrobotics.kapuchin.subsystems.shooter

import com.lynbrookrobotics.kapuchin.Subsystems.Companion.pneumaticTicker
import com.lynbrookrobotics.kapuchin.Subsystems.Companion.sharedTickerTiming
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import edu.wpi.first.wpilibj.Relay
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

typealias FlashlightState = Relay.Value

class FlashlightComponent(hardware: FlashlightHardware) : Component<FlashlightComponent, FlashlightHardware, FlashlightState>(hardware, pneumaticTicker) {

    override val fallbackController: FlashlightComponent.(Time) -> FlashlightState = { FlashlightState.kOff }

    override fun FlashlightHardware.output(value: FlashlightState) {
        flashlightRelay.set(value)
    }
}

class FlashlightHardware : SubsystemHardware<FlashlightHardware, FlashlightComponent>() {
    override val period by sharedTickerTiming
    override val syncThreshold = 50.milli(Second)
    override val priority = Priority.Low
    override val name = "Intake Slider"

    private val relayChannel = 0
    val flashlightRelay by hardw { Relay(relayChannel) }
}