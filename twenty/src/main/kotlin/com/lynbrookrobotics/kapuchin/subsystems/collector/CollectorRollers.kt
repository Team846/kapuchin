package com.lynbrookrobotics.kapuchin.subsystems.collector

import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.revrobotics.CANSparkMax
import com.revrobotics.CANSparkMaxLowLevel.MotorType.kBrushless
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class CollectorRollersComponent(hardware: CollectorRollersHardware) : Component<CollectorRollersComponent, CollectorRollersHardware, OffloadedOutput>(hardware) {
    val cargoCollectSpeed by pref(11, Volt)
    val cargoReleaseSpeed by pref(-6, Volt)

    override val fallbackController: CollectorRollersComponent.(Time) -> OffloadedOutput = {
        PercentOutput(hardware.escConfig, 0.Percent)
    }

    override fun CollectorRollersHardware.output(value: OffloadedOutput) {
        value.writeTo(rollersEsc)
    }
}

class CollectorRollersHardware : SubsystemHardware<CollectorRollersHardware, CollectorRollersComponent>() {
    override val priority: Priority = Priority.Low
    override val period: Time = 50.milli(Second)
    override val syncThreshold: Time = 20.milli(Second)
    override val name: String = "Collector Rollers"

    private val invertRollers by pref(false)

    val escConfig by escConfigPref(
            defaultNominalOutput = 0.5.Volt,

            defaultContinuousCurrentLimit = 25.Ampere,
            defaultPeakCurrentLimit = 35.Ampere
    )

    private val rollersEscId by pref(0)
    val rollersEsc by hardw { CANSparkMax(rollersEscId, kBrushless) }
}
