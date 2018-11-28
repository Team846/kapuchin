package com.lynbrookrobotics.kapuchin.subsystems

import com.lynbrookrobotics.kapuchin.control.data.stampWith
import com.lynbrookrobotics.kapuchin.control.loops.Gain
import com.lynbrookrobotics.kapuchin.control.loops.pid.PidGains
import com.lynbrookrobotics.kapuchin.hardware.HardwareInit.Companion.hardw
import com.lynbrookrobotics.kapuchin.preferences.pref
import com.lynbrookrobotics.kapuchin.timing.Priority
import edu.wpi.first.wpilibj.Counter
import edu.wpi.first.wpilibj.Spark
import info.kunalsheth.units.generated.*

class ShooterComponent(hardware: ShooterHardware) : Component<ShooterComponent, ShooterHardware, Pair<Dimensionless, Dimensionless>>(hardware) {

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
        frontEsc.set(value.first.siValue)
        backEsc.set(value.second.siValue)
    }
}

class ShooterHardware : SubsystemHardware<ShooterHardware, ShooterComponent>() {
    override val name = "Shooter"
    override val priority = Priority.RealTime
    override val period = 10.milli(Second)
    override val syncThreshold = 1.milli(Second)

    val frontHallEffectPort by pref(0)
    val backHallEffectPort by pref(1)
    val frontHallEffect by hardw { Counter(frontHallEffectPort) }.sensor {
        180.Degree / period.Second stampWith it
    }
    val backHallEffect by hardw { Counter(backHallEffectPort) }.sensor {
        180.Degree / period.Second stampWith it
    }

    val frontEscPort by pref(0)
    val backEscPort by pref(1)
    val frontEsc by hardw { Spark(frontEscPort) }
    val backEsc by hardw { Spark(backEscPort) }
}

