package com.lynbrookrobotics.twenty.subsystems.shooter

import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.twenty.Subsystems
import edu.wpi.first.wpilibj.DigitalOutput
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

enum class FlashlightState(val output: Boolean) { Off(false), On(true) }

class FlashlightComponent(hardware: FlashlightHardware) :
    Component<FlashlightComponent, FlashlightHardware, FlashlightState>(hardware, Subsystems.uiTicker) {

    override val fallbackController: FlashlightComponent.(Time) -> FlashlightState = { FlashlightState.Off }

    override fun FlashlightHardware.output(value: FlashlightState) {
        flashlightRelay.set(value.output)
    }
}

class FlashlightHardware : SubsystemHardware<FlashlightHardware, FlashlightComponent>() {
    override val period by Subsystems.sharedTickerTiming
    override val syncThreshold = 50.milli(Second)
    override val priority = Priority.Low
    override val name = "Flashlight"

    private val flashlightPort = 0
    val flashlightRelay by hardw { DigitalOutput(flashlightPort) }
}