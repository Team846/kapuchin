package com.lynbrookrobotics.kapuchin.subsystems.storage

import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.revrobotics.CANPIDController
import com.revrobotics.CANSparkMax
import com.revrobotics.CANSparkMaxLowLevel.MotorType
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class FeederRollerComponent(hardware: FeederRollerHardware) : Component<FeederRollerComponent, FeederRollerHardware, OffloadedOutput>(hardware) {

    override val fallbackController: FeederRollerComponent.(Time) -> OffloadedOutput = {
        PercentOutput(hardware.escConfig, 0.Percent)
    }

    override fun FeederRollerHardware.output(value: OffloadedOutput) {
        value.writeTo(feederRollerEsc, feederRollerPidController)
    }
}

class FeederRollerHardware : SubsystemHardware<FeederRollerHardware, FeederRollerComponent>() {
    override val period = 50.milli(Second)
    override val syncThreshold = 20.milli(Second)
    override val priority = Priority.Medium
    override val name = "Feeder Roller"

    private val feederRollerEscId = 55

    val escConfig by escConfigPref(
            defaultNominalOutput = 0.5.Volt,

            defaultContinuousCurrentLimit = 25.Ampere,
            defaultPeakCurrentLimit = 35.Ampere
    )

    val feederRollerEsc by hardw { CANSparkMax(feederRollerEscId, MotorType.kBrushless) } // 550?
    val feederRollerPidController: CANPIDController by hardw { feederRollerEsc.pidController }

    val escGains by pref {
        val kP by pref(0.0)
        val kI by pref(0.0)
        val kD by pref(0.0)
        val kF by pref(0.0)
        val maxIntegralAccumulator by pref(0.0)

        ({ OffloadedEscGains(syncThreshold, kP, kI, kD, kF, maxIntegralAccumulator) })
    }

}
