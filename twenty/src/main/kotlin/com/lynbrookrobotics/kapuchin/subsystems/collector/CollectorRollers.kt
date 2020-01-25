package com.lynbrookrobotics.kapuchin.subsystems.collector

import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.revrobotics.CANSparkMax
import com.revrobotics.CANSparkMaxLowLevel.MotorType.kBrushless
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class CollectorRollersComponent(hardware: CollectorRollersHardware) : Component<CollectorRollersComponent, CollectorRollersHardware, DutyCycle>(hardware) {
    val fallbackValue by pref(0, Percent)
    val CollectSpeed by pref(11, Volt)
    val ReleaseSpeed by pref(-6, Volt)


    override val fallbackController: CollectorRollersComponent.(Time) -> DutyCycle = { fallbackValue }

    override fun CollectorRollersHardware.output(value: DutyCycle) {
        collectorEsc.set(value.Each)
    }
}

class CollectorRollersHardware : SubsystemHardware<CollectorRollersHardware, CollectorRollersComponent>() {
    override val priority: Priority = Priority.Medium
    override val period: Time = 50.milli(Second)
    override val syncThreshold: Time = 20.milli(Second)
    override val name: String = "Collector Rollers"

    private val collectorEscId by pref(10)
    val escConfig by escConfigPref(
            defaultNominalOutput = 0.5.Volt,

            defaultContinuousCurrentLimit = 25.Ampere,
            defaultPeakCurrentLimit = 35.Ampere
    )

    val collectorEsc by hardw { CANSparkMax(collectorEscId, kBrushless) }


}
