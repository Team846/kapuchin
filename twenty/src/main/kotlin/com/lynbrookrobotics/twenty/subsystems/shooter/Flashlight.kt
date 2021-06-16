package com.lynbrookrobotics.twenty.subsystems.shooter

import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.twenty.Subsystems.Companion.sharedTickerTiming
import com.lynbrookrobotics.twenty.Subsystems.Companion.uiBaselineTicker
import com.lynbrookrobotics.twenty.subsystems.shooter.FlashlightState.Off
import edu.wpi.first.wpilibj.DigitalOutput
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

enum class FlashlightState(val output: Boolean) { Off(false), On(true) }

class FlashlightComponent(hardware: FlashlightHardware) :
    Component<FlashlightComponent, FlashlightHardware, FlashlightState>(hardware, uiBaselineTicker) {

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