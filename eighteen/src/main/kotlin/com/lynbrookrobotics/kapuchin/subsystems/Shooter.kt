package com.lynbrookrobotics.kapuchin.subsystems

import com.lynbrookrobotics.kapuchin.control.electrical.voltageToDutyCycle
import com.lynbrookrobotics.kapuchin.control.loops.Gain
import com.lynbrookrobotics.kapuchin.control.stampWith
import com.lynbrookrobotics.kapuchin.hardware.hardw
import com.lynbrookrobotics.kapuchin.preferences.pref
import com.lynbrookrobotics.kapuchin.timing.Priority
import edu.wpi.first.wpilibj.Counter
import edu.wpi.first.wpilibj.Spark
import info.kunalsheth.units.generated.*

class ShooterComponent(hardware: ShooterHardware, es: ElectricalSystemHardware) : Component<ShooterComponent, ShooterHardware, Pair<Volt, Volt>>(hardware) {
    val kP: Gain<Volt, AngularVelocity> by pref {
        val compensation by pref(12::Volt)
        val forError by pref(3000::Rpm)
        fun() = Gain(compensation, forError)
    }

    val topSpeed by pref(6500.0::Rpm)
    val kF = Gain(12.Volt, topSpeed)

    val battery by es.batteryVoltage

    override val fallbackController: ShooterComponent.(Time) -> Pair<Volt, Volt> = { 0.Volt to 0.Volt }

    override fun ShooterHardware.output(value: Pair<Volt, Volt>) {
        frontEsc.set(voltageToDutyCycle(value.first, battery.value).Tick)
        backEsc.set(voltageToDutyCycle(value.first, battery.value).Tick)
    }
}

class ShooterHardware : SubsystemHardware<ShooterHardware, ShooterComponent>() {
    override val subsystemName = "Shooter"
    override val priority = Priority.RealTime
    override val period = 10.milli(::Second)
    override val syncThreshold = 0.5.milli(::Second)

    val frontHallEffectPort by pref(0)
    val backHallEffectPort by pref(1)
    val frontHallEffect by hardw { Counter(frontHallEffectPort) }.readWithComponent {
        180.Degree / period.Second stampWith it
    }
    val backHallEffect by hardw { Counter(backHallEffectPort) }.readWithComponent {
        180.Degree / period.Second stampWith it
    }

    val frontEscPort by pref(0)
    val backEscPort by pref(1)
    val frontEsc by hardw { Spark(frontEscPort) }
    val backEsc by hardw { Spark(backEscPort) }
}

