package com.lynbrookrobotics.kapuchin.subsystems

import com.lynbrookrobotics.kapuchin.control.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import edu.wpi.first.wpilibj.Spark
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class ClimberComponent(hardware: ClimberHardware) : Component<ClimberComponent, ClimberHardware, DutyCycle>(hardware, EventLoop) {

    override val fallbackController: ClimberComponent.(Time) -> DutyCycle = { 0.Percent }

    val maxOutput by pref(20, Percent)

    override fun ClimberHardware.output(value: DutyCycle) {
        val safeOutput = value minMag maxOutput
        hardware.leftEsc.set(safeOutput.Each)
        hardware.rightEsc.set(safeOutput.Each)
    }

}

class ClimberHardware : SubsystemHardware<ClimberHardware, ClimberComponent>() {
    override val priority: Priority = Priority.Low
    override val period: Time = 250.milli(Second)
    override val syncThreshold: Time = 20.milli(Second)
    override val name: String = "Climber"

    val leftEscPort by pref(4)
    val leftEsc by hardw { Spark(leftEscPort) }

    val rightEscPort by pref(5)
    val rightEsc by hardw { Spark(rightEscPort) }
}