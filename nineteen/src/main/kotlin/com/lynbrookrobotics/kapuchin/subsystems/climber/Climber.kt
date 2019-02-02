package com.lynbrookrobotics.kapuchin.subsystems.climber

import com.lynbrookrobotics.kapuchin.control.data.TwoSided
import com.lynbrookrobotics.kapuchin.hardware.HardwareInit.Companion.hardw
import com.lynbrookrobotics.kapuchin.preferences.pref
import com.lynbrookrobotics.kapuchin.subsystems.SubsystemHardware
import com.lynbrookrobotics.kapuchin.timing.Priority
import info.kunalsheth.units.generated.DutyCycle
import info.kunalsheth.units.generated.Time
import com.lynbrookrobotics.kapuchin.subsystems.Component
import edu.wpi.first.wpilibj.Spark
import info.kunalsheth.units.generated.Percent
import info.kunalsheth.units.generated.Second
import info.kunalsheth.units.math.milli

class ClimberComponent(hardware: ClimberHardware) : Component<ClimberComponent, ClimberHardware, TwoSided<DutyCycle>>(hardware) {
    override val fallbackController: ClimberComponent.(Time) -> TwoSided<DutyCycle> = { TwoSided(0.Percent, 0.Percent) }

    override fun ClimberHardware.output(value: TwoSided<DutyCycle>) {
        hardware.escLeft.set(value.left.siValue)
        hardware.escRight.set(value.right.siValue)
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