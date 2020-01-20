package com.lynbrookrobotics.kapuchin.subsystems.climber

import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.revrobotics.CANSparkMax
import com.revrobotics.CANSparkMaxLowLevel.MotorType.kBrushed
import info.kunalsheth.units.generated.*

class BarAdjustmentComponent(hardware: BarAdjustmentHardware) : Component<BarAdjustmentComponent, BarAdjustmentHardware, DutyCycle>(hardware) {
    override val fallbackController: BarAdjustmentComponent.(Time) -> DutyCycle = { 0.Percent }

    override fun BarAdjustmentHardware.output(value: DutyCycle) {
        barAdjustmentEsc.set(value.Each)
    }
}

class BarAdjustmentHardware : SubsystemHardware<BarAdjustmentHardware, BarAdjustmentComponent>() {
    override val period: Time = 30.Millisecond
    override val syncThreshold: Time = 30.Millisecond
    override val priority: Priority = Priority.Low
    override val name: String = "Bar Adjustment"

    private val barAdjustmentEscId by pref(10)

    val barAdjustmentEsc by hardw { CANSparkMax(barAdjustmentEscId, kBrushed) }
}