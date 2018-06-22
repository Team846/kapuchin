package com.lynbrookrobotics.kapuchin.subsystems

import com.lynbrookrobotics.kapuchin.control.electrical.voltageToDutyCycle
import com.lynbrookrobotics.kapuchin.control.loops.Gain
import com.lynbrookrobotics.kapuchin.delegates.preferences.pref
import com.lynbrookrobotics.kapuchin.delegates.sensors.withComponentSensor
import com.lynbrookrobotics.kapuchin.hardware.dsl.hardw
import com.lynbrookrobotics.kapuchin.timing.Priority
import edu.wpi.first.wpilibj.Counter
import edu.wpi.first.wpilibj.Spark
import info.kunalsheth.units.generated.*

class ShooterComponent(hardware: ShooterHardware, es: ElectricalSystemComponent) : Component<ShooterComponent, ShooterHardware, Pair<Volt, Volt>>(hardware) {
    val frontVelocity by withComponentSensor {
        180.Degree / frontHallEffect.period.Second stampWith it
    }

    val backVelocity by withComponentSensor {
        180.Degree / backHallEffect.period.Second stampWith it
    }

    val kP: Gain<Volt, AngularVelocity> by pref {
        val compensation by pref(12::Volt)
        val forError by pref(3000::Rpm)
        fun() = Gain(compensation, forError)
    }

    val topSpeed by pref(6500.0::Rpm)
    val kF = Gain(12.Volt, topSpeed)

    val battery by es.batterySensor

    override val fallbackController: ShooterComponent.(Time) -> Pair<Volt, Volt> = { 0.Volt to 0.Volt }

    override fun ShooterHardware.output(value: Pair<Volt, Volt>) {
        frontEsc.set(voltageToDutyCycle(value.first, battery.value).DutyCycle)
        backEsc.set(voltageToDutyCycle(value.first, battery.value).DutyCycle)
    }
}

class ShooterHardware : SubsystemHardware<ShooterHardware, ShooterComponent>() {
    override val subsystemName = "Shooter"
    override val priority = Priority.RealTime
    override val period = 10.milli(::Second)
    override val syncThreshold = 0.5.milli(::Second)

    val frontHallEffectPort by pref(0)
    val backHallEffectPort by pref(1)
    val frontHallEffect by hardw { Counter(frontHallEffectPort) }
    val backHallEffect by hardw { Counter(backHallEffectPort) }

    val frontEscPort by pref(0)
    val backEscPort by pref(1)
    val frontEsc by hardw { Spark(frontEscPort) }
    val backEsc by hardw { Spark(backEscPort) }
}

