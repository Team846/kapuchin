package com.lynbrookrobotics.kapuchin.subsystems.intake.collector.hookslider

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.collector.hookslider.HookSliderState.*
import com.lynbrookrobotics.kapuchin.timing.*
import edu.wpi.first.wpilibj.Solenoid
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class HookSliderComponent(hardware: HookSliderHardware) : Component<HookSliderComponent, HookSliderHardware, HookSliderState>(hardware, Subsystems.pneumaticTicker) {

    override val fallbackController: HookSliderComponent.(Time) -> HookSliderState = { In }

    override fun HookSliderHardware.output(value: HookSliderState) {

        solenoid.set(value.output)

//        val legal = HookSliderState.legalRanges()
//
//        when {
//            !legal.any() -> log(Warning) { "No $name legal states found" }
//            value == HookSliderState.Undetermined -> log(Warning) { "Illegal $name state inputted" }
//            value in legal || HookSliderState.Undetermined in legal -> solenoid.set(value.output)
//            else -> Unit
//        }
    }
}

class HookSliderHardware : SubsystemHardware<HookSliderHardware, HookSliderComponent>() {
    override val priority: Priority = Priority.Low
    override val period: Time = 100.milli(Second)
    override val syncThreshold: Time = 20.milli(Second)
    override val name: String = "Hook Slider"

    val solenoidPort = 2
    val solenoid = Solenoid(solenoidPort)
}
