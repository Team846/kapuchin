package com.lynbrookrobotics.kapuchin.subsystems.intake

import com.lynbrookrobotics.kapuchin.Subsystems.Companion.pneumaticTicker
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.IntakeSliderState.*
import com.lynbrookrobotics.kapuchin.timing.*
import edu.wpi.first.wpilibj.Solenoid
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

enum class IntakeSliderState(val output: Boolean) { Out(true), In(false) }

class IntakeSliderComponent(hardware: IntakeSliderHardware) : Component<IntakeSliderComponent, IntakeSliderHardware, IntakeSliderState>(hardware, pneumaticTicker) {

    override val fallbackController: IntakeSliderComponent.(Time) -> IntakeSliderState = { In }

    override fun IntakeSliderHardware.output(value: IntakeSliderState) {
        solenoid.set(value.output)
    }
}

class IntakeSliderHardware : SubsystemHardware<IntakeSliderHardware, IntakeSliderComponent>() {
    override val period: Time = 100.milli(Second)
    override val syncThreshold: Time = 20.milli(Second)
    override val priority: Priority = Priority.Low
    override val name: String = "Intake Slider"

    private val solenoidPort = 3

    val solenoid by hardw { Solenoid(solenoidPort) }
}