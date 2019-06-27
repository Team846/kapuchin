package com.lynbrookrobotics.kapuchin.subsystems.collector

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.collector.HookSliderState.*
import com.lynbrookrobotics.kapuchin.timing.*
import edu.wpi.first.wpilibj.Solenoid
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

enum class HookSliderState(val output: Boolean) { In(false), Out(true) }

class HookSlider(hardware: HookSliderHardware) : Component<HookSlider, HookSliderHardware, HookSliderState>(hardware, Subsystems.pneumaticTicker) {

    override val fallbackController: HookSlider.(Time) -> HookSliderState = { In }

    override fun HookSliderHardware.output(value: HookSliderState) {
        solenoid.set(value.output)
    }
}

class HookSliderHardware : SubsystemHardware<HookSliderHardware, HookSlider>() {
    override val priority: Priority = Priority.Low
    override val period: Time = 100.milli(Second)
    override val syncThreshold: Time = 20.milli(Second)
    override val name: String = "Hook Slider"

    val solenoidPort = 1
    val solenoid = Solenoid(solenoidPort)
}
