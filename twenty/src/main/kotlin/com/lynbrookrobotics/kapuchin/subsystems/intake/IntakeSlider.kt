package com.lynbrookrobotics.kapuchin.subsystems.intake

import com.lynbrookrobotics.kapuchin.Subsystems.Companion.pneumaticTicker
import com.lynbrookrobotics.kapuchin.Subsystems.Companion.sharedTickerTiming
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.IntakeSliderState.*
import com.lynbrookrobotics.kapuchin.timing.*
import edu.wpi.first.wpilibj.Solenoid
import info.kunalsheth.units.generated.*

enum class IntakeSliderState(val output: Boolean) { Out(true), In(false) }

class IntakeSliderComponent(hardware: IntakeSliderHardware) : Component<IntakeSliderComponent, IntakeSliderHardware, IntakeSliderState>(hardware, pneumaticTicker) {

    override val fallbackController: IntakeSliderComponent.(Time) -> IntakeSliderState = { In }

    override fun IntakeSliderHardware.output(value: IntakeSliderState) {
        sliderSolenoid.set(value.output)
    }
}

class IntakeSliderHardware : SubsystemHardware<IntakeSliderHardware, IntakeSliderComponent>() {
    override val period = sharedTickerTiming()
    override val syncThreshold = sharedTickerTiming()
    override val priority = Priority.Low
    override val name = "Intake Slider"

    private val sliderSolenoidChannel = 3

    val sliderSolenoid by hardw { Solenoid(sliderSolenoidChannel) }
}