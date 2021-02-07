package com.lynbrookrobotics.eighteen.subsystems

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.loops.*
import com.lynbrookrobotics.kapuchin.control.loops.pid.*
import com.lynbrookrobotics.kapuchin.hardware.HardwareInit.Companion.hardw
import com.lynbrookrobotics.kapuchin.hardware.Sensor.Companion.sensor
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import edu.wpi.first.wpilibj.Counter
import edu.wpi.first.wpilibj.Spark
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class ShooterComponent(hardware: ShooterHardware) : Component<ShooterComponent, ShooterHardware, Pair<DutyCycle, DutyCycle>>(hardware) {

    val topSpeed by pref(6500.0, Rpm)
    val gains by pref {
        val kP by pref(12, Volt, 2000, Rpm)
        val kI by pref(0, Volt, 1, Turn)
        val kD by pref(0, Volt, 1, DegreePerSecondSquared)
        val kF = Gain(12.Volt, topSpeed)
        ({ PidGains(kP, kI, kD, kF) })
    }

    override val fallbackController: ShooterComponent.(Time) -> Pair<Dimensionless, Dimensionless> = { 0.Percent to 0.Percent }

    override fun ShooterHardware.output(value: Pair<Dimensionless, Dimensionless>) {
        frontEsc.set(value.first.Each)
        backEsc.set(value.second.Each)
    }
}

class ShooterHardware : SubsystemHardware<ShooterHardware, ShooterComponent>() {
    override val name = "Shooter"
    override val priority = Priority.RealTime
    override val period = 10.milli(Second)
    override val syncThreshold = 1.milli(Second)

    val frontHallEffectPort by pref(0)
    val backHallEffectPort by pref(1)

    val frontHallEffect by hardw { Counter(frontHallEffectPort) }
    val backHallEffect by hardw { Counter(backHallEffectPort) }

    val frontVelocity = sensor(frontHallEffect) { 180.Degree / period.Second stampWith it }
    val backVelocity = sensor(backHallEffect) { 180.Degree / period.Second stampWith it }

    val frontEscPort by pref(0)
    val backEscPort by pref(1)
    val frontEsc by hardw { Spark(frontEscPort) }
    val backEsc by hardw { Spark(backEscPort) }
}

