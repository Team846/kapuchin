package com.lynbrookrobotics.kapuchin.subsystems.shooter

import com.lynbrookrobotics.kapuchin.Subsystems.Companion.pneumaticTicker
import com.lynbrookrobotics.kapuchin.Subsystems.Companion.sharedTickerTiming
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.shooter.FlashlightState.*
import com.lynbrookrobotics.kapuchin.timing.*
import edu.wpi.first.wpilibj.DigitalOutput
import edu.wpi.first.wpilibj.Relay
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

enum class FlashlightState(val output: Boolean) { Off(false), On(true) }

class FlashlightComponent(hardware: FlashlightHardware) : Component<FlashlightComponent, FlashlightHardware, FlashlightState>(hardware, pneumaticTicker) {

    override val fallbackController: FlashlightComponent.(Time) -> FlashlightState = { Off }

    override fun FlashlightHardware.output(value: FlashlightState) {
        flashlightRelay.set(value.output)
    }
}

class FlashlightHardware : SubsystemHardware<FlashlightHardware, FlashlightComponent>() {
    override val period by sharedTickerTiming
    override val syncThreshold = 50.milli(Second)
    override val priority = Priority.Low
    override val name = "Flashlight"

    private val flashlightPort = 0
    val flashlightRelay by hardw { DigitalOutput(flashlightPort) }
}