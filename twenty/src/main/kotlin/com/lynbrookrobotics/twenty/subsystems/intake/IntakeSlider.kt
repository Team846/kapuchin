package com.lynbrookrobotics.twenty.subsystems.intake

import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.twenty.Subsystems
import edu.wpi.first.wpilibj.Solenoid
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

enum class IntakeSliderState(val output: Boolean) { Out(true), In(false) }

class IntakeSliderComponent(hardware: IntakeSliderHardware) :
    Component<IntakeSliderComponent, IntakeSliderHardware, IntakeSliderState>(hardware, Subsystems.pneumaticTicker) {

    override val fallbackController: IntakeSliderComponent.(Time) -> IntakeSliderState = { IntakeSliderState.In }

    override fun IntakeSliderHardware.output(value: IntakeSliderState) {
        sliderSolenoid.set(value.output)
    }
}

class IntakeSliderHardware : SubsystemHardware<IntakeSliderHardware, IntakeSliderComponent>() {
    override val period = Subsystems.pneumaticTicker.period
    override val syncThreshold = 50.milli(Second)
    override val priority = Priority.Low
    override val name = "Intake Slider"

    private val sliderSolenoidChannel = 3

    val sliderSolenoid by hardw { Solenoid(sliderSolenoidChannel) }
}