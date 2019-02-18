package com.lynbrookrobotics.kapuchin.subsystems

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.timing.*
import edu.wpi.first.wpilibj.Spark
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class ClimberComponent(hardware: ClimberHardware) : Component<ClimberComponent, ClimberHardware, TwoSided<DutyCycle>>(hardware) {
    override val fallbackController: ClimberComponent.(Time) -> TwoSided<DutyCycle> = { TwoSided(0.Percent, 0.Percent) }

    override fun ClimberHardware.output(value: TwoSided<DutyCycle>) {
        hardware.escLeft.set(value.left.Each)
        hardware.escRight.set(value.right.Each)
    }

}

class ClimberHardware : SubsystemHardware<ClimberHardware, ClimberComponent>() {
    override val priority: Priority = Priority.Low
    override val period: Time = 250.milli(Second)
    override val syncThreshold: Time = 10.milli(Second)
    override val name: String = "Climber"

    val leftEscChannel by pref(1)
    val escLeft by hardw { Spark(leftEscChannel) }

    val rightEscChannel by pref(2)
    val escRight by hardw { Spark(rightEscChannel) }
}