package com.lynbrookrobotics.kapuchin.subsystems.intake.collector

import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.can.VictorSPX
import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import edu.wpi.first.wpilibj.Spark
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class CollectorRollersComponent(hardware: CollectorRollersHardware) : Component<CollectorRollersComponent, CollectorRollersHardware, TwoSided<DutyCycle>>(hardware, Subsystems.pneumaticTicker) {

    val cargoHoldStrength by pref(33, Percent)
    val cargoCollectSpeed by pref(50, Percent)
    val cargoReleaseSpeed by pref(-50, Percent)

    override val fallbackController: CollectorRollersComponent.(Time) -> TwoSided<DutyCycle> = { TwoSided(-cargoHoldStrength) }

    override fun CollectorRollersHardware.output(value: TwoSided<DutyCycle>) {
        topEsc.set(value.left.Each)
        botEsc.set(ControlMode.PercentOutput, value.right.Each)
    }

}

class CollectorRollersHardware : SubsystemHardware<CollectorRollersHardware, CollectorRollersComponent>() {
    override val priority: Priority = Priority.Low
    override val period: Time = 50.milli(Second)
    override val syncThreshold: Time = 20.milli(Second)
    override val name: String = "Collector Rollers"

    val topPwmPort by pref(0)
    val topEsc by hardw { Spark(topPwmPort) }

    val botCanID by pref(50)
    val botEsc by hardw { VictorSPX(botCanID) }.configure { it.inverted = true }
}
