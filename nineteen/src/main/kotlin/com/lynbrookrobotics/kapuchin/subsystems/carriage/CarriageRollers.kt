package com.lynbrookrobotics.kapuchin.subsystems.carriage

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.hardware.HardwareInit.Companion.hardw
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import edu.wpi.first.wpilibj.Spark
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class CarriageRollersComponent(hardware: CarriageRollersHardware) : Component<CarriageRollersComponent, CarriageRollersHardware, TwoSided<DutyCycle>>(hardware, EventLoop) {

    val cargoHoldStrength by pref(33, Percent)

    override val fallbackController: CarriageRollersComponent.(Time) -> TwoSided<DutyCycle> = { TwoSided(-cargoHoldStrength) }

    override fun CarriageRollersHardware.output(value: TwoSided<DutyCycle>) {
        topEsc.set(value.left.Each)
        botEsc.set(value.right.Each)
    }

}

class CarriageRollersHardware : SubsystemHardware<CarriageRollersHardware, CarriageRollersComponent>() {
    override val priority: Priority = Priority.Low
    override val period: Time = 100.milli(Second)
    override val syncThreshold: Time = 50.milli(Second)
    override val name: String = "Rollers"

    val topPwmPort by pref(0)
    val topEsc by hardw { Spark(topPwmPort) }

    val bottomPwmPort by pref(1)
    val botEsc by hardw { Spark(bottomPwmPort) }.configure { it.inverted = true }
}