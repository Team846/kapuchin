package com.lynbrookrobotics.kapuchin.subsystems.intake.handoff

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import edu.wpi.first.wpilibj.Spark
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class HandoffRollersComponent(hardware: HandoffRollersHardware) : Component<HandoffRollersComponent, HandoffRollersHardware, TwoSided<DutyCycle>>(hardware, Subsystems.pneumaticTicker) {

    val cargoHoldStrength by pref(33, Percent)
    val cargoCollectSpeed by pref(50, Percent)

    private val fallbackValue = TwoSided(0.Percent)
    override val fallbackController: HandoffRollersComponent.(Time) -> TwoSided<DutyCycle> = { fallbackValue }

    override fun HandoffRollersHardware.output(value: TwoSided<DutyCycle>) {
        leftEsc.set(value.left.Each)
        rightEsc.set(value.right.Each)
    }

}

class HandoffRollersHardware : SubsystemHardware<HandoffRollersHardware, HandoffRollersComponent>() {
    override val priority: Priority = Priority.Low
    override val period: Time = 100.milli(Second)
    override val syncThreshold: Time = 20.milli(Second)
    override val name: String = "Handoff Rollers"

    val invertLeft by pref(false)
    val invertRight by pref(true)

    val leftPwmPort = 1
    val leftEsc by hardw { Spark(leftPwmPort) }.configure { it.inverted = invertLeft }

    val rightPwmPort = 5
    val rightEsc by hardw { Spark(rightPwmPort) }.configure { it.inverted = invertRight }
}
