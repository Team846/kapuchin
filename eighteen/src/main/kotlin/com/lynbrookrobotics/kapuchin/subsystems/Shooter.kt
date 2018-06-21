package com.lynbrookrobotics.kapuchin.subsystems

import com.lynbrookrobotics.kapuchin.control.electrical.voltageToDutyCycle
import com.lynbrookrobotics.kapuchin.control.loops.Gain
import com.lynbrookrobotics.kapuchin.control.stampWith
import com.lynbrookrobotics.kapuchin.delegates.preferences.preference
import com.lynbrookrobotics.kapuchin.delegates.sensors.withComponentSensor
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

    val kP: Gain<Volt, AngularVelocity> by preference {
        val a = 1
        { Gain(12.Volt, 3000.Rpm) }
    }

    val topSpeed by preference(6500.0::Rpm)
    val kF = Gain(12.Volt, topSpeed)

    val battery by es.batterySensor

    override val fallbackController: ShooterComponent.(Time) -> Pair<Volt, Volt> = { 0.Volt to 0.Volt }

    override fun ShooterHardware.output(value: Pair<Volt, Volt>) {
        frontEsc.set(voltageToDutyCycle(value.first, battery.value).DutyCycle)
        backEsc.set(voltageToDutyCycle(value.first, battery.value).DutyCycle)
    }
}

class ShooterHardware : Hardware<ShooterHardware, ShooterComponent>() {
    override val name = "Shooter"
    override val priority = Priority.RealTime
    override val period = 10.milli(::Second)
    override val syncThreshold = 0.5.milli(::Second)

    val frontHallEffectPort by preference(0)
    val backHallEffectPort by preference(1)
    val frontHallEffect = Counter(frontHallEffectPort)
    val backHallEffect = Counter(backHallEffectPort)

    val frontEscPort by preference(0)
    val backEscPort by preference(1)
    val frontEsc = Spark(frontEscPort)
    val backEsc = Spark(backEscPort)
}

