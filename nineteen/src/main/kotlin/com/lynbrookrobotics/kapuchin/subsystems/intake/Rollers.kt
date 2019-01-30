package com.lynbrookrobotics.kapuchin.subsystems.intake

import com.lynbrookrobotics.kapuchin.control.data.TwoSided
import com.lynbrookrobotics.kapuchin.hardware.HardwareInit.Companion.hardw
import com.lynbrookrobotics.kapuchin.preferences.pref
import com.lynbrookrobotics.kapuchin.subsystems.Component
import com.lynbrookrobotics.kapuchin.subsystems.SubsystemHardware
import com.lynbrookrobotics.kapuchin.timing.Priority
import edu.wpi.first.wpilibj.Spark
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.milli

class RollersComponent(hardware: RollersHardware) : Component<RollersComponent, RollersHardware, TwoSided<DutyCycle>>(hardware) {

    val cargoHoldStrength by pref(33, Percent)

    override val fallbackController: RollersComponent.(Time) -> TwoSided<DutyCycle> = { TwoSided(-cargoHoldStrength) }

    override fun RollersHardware.output(value: TwoSided<DutyCycle>) {
        leftEsc.set(value.left.Each)
        rightEsc.set(value.right.Each)
    }

}

class RollersHardware : SubsystemHardware<RollersHardware, RollersComponent>() {
    override val priority: Priority = Priority.Low
    override val period: Time = 100.milli(Second)
    override val syncThreshold: Time = 50.milli(Second)
    override val name: String = "Rollers"

    val leftPwmPort by pref(1)
    val leftEsc by hardw { Spark(leftPwmPort) }

    val rightPwmPort by pref(0)
    val rightEsc by hardw { Spark(rightPwmPort) }.configure { it.inverted = true }
}