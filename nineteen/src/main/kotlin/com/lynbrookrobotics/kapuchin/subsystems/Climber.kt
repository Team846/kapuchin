package com.lynbrookrobotics.kapuchin.subsystems

import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import edu.wpi.first.wpilibj.Spark
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class ClimberComponent(hardware: ClimberHardware) : Component<ClimberComponent, ClimberHardware, DutyCycle>(hardware, EventLoop) {

    override val fallbackController: ClimberComponent.(Time) -> DutyCycle = { 0.Percent }

    val maxOutput by pref(80, Percent)
    val invert by pref(true)

    override fun ClimberHardware.output(value: DutyCycle) {
        val safeOutput =
                if (value in `±`(maxOutput)) value
                else maxOutput * value.signum
        val invertedOutput = if(invert) -safeOutput else safeOutput

        hardware.leftEsc.set(invertedOutput.Each)
        hardware.rightEsc.set(invertedOutput.Each)
    }
}

class ClimberHardware : SubsystemHardware<ClimberHardware, ClimberComponent>() {
    override val priority: Priority = Priority.Low
    override val period: Time = 250.milli(Second)
    override val syncThreshold: Time = 20.milli(Second)
    override val name: String = "Climber"

    val leftPwmPort = 8
    val leftEsc by hardw { Spark(leftPwmPort) }

    val rightPwmPort = 9
    val rightEsc by hardw { Spark(rightPwmPort) }
}