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

    val cargoHoldStrength by pref(20, Percent)
    val cargoCollectSpeed by pref(11, Volt)
    val cargoCenterSpeed by pref(8.5, Volt)
    val cargoReleaseSpeed by pref(-6, Volt)

    val hatchState = TwoSided(
            cargoHoldStrength,
            -cargoHoldStrength
    )
    val cargoState = TwoSided(
            cargoHoldStrength
    )

    private val fallbackValue = TwoSided(
            cargoHoldStrength,
            0.Percent
    )
    override val fallbackController: CollectorRollersComponent.(Time) -> TwoSided<DutyCycle> = { fallbackValue }

    override fun CollectorRollersHardware.output(value: TwoSided<DutyCycle>) {
        topEsc.set(value.left.Each)
        bottomEsc.set(ControlMode.PercentOutput, value.right.Each)
    }
}

class CollectorRollersHardware : SubsystemHardware<CollectorRollersHardware, CollectorRollersComponent>() {
    override val priority: Priority = Priority.Low
    override val period: Time = 50.milli(Second)
    override val syncThreshold: Time = 20.milli(Second)
    override val name: String = "Collector Rollers"

    private val invertTop by pref(false)
    private val invertBottom by pref(false)

    val topPwmPort = 0
    val topEsc by hardw { Spark(topPwmPort) }.configure {
        it.inverted = invertTop
    }

    val bottomCanId = 50
    val bottomEsc by hardw { VictorSPX(bottomCanId) }.configure {
        generalSetup(it, 12.Volt, 20.Ampere, 0.Volt)
        it.inverted = invertBottom
    }
}
