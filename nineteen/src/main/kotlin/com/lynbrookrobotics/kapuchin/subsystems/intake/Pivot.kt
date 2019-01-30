package com.lynbrookrobotics.kapuchin.subsystems.intake

import com.lynbrookrobotics.kapuchin.hardware.HardwareInit.Companion.hardw
import com.lynbrookrobotics.kapuchin.preferences.pref
import com.lynbrookrobotics.kapuchin.subsystems.Component
import com.lynbrookrobotics.kapuchin.subsystems.SubsystemHardware
import com.lynbrookrobotics.kapuchin.timing.Priority
import edu.wpi.first.wpilibj.Solenoid
import info.kunalsheth.units.generated.Second
import info.kunalsheth.units.generated.Time
import info.kunalsheth.units.math.milli

enum class PivotPosition {
    PivotUp, PivotDown
}

class PivotComponent(hardware: PivotHardware) : Component<PivotComponent, PivotHardware, PivotPosition>(hardware) {
    override val fallbackController: PivotComponent.(Time) -> PivotPosition = {PivotPosition.PivotDown}

    override fun PivotHardware.output(value: PivotPosition) {
        when(value) {
            PivotPosition.PivotDown -> solenoid.set(false)
            PivotPosition.PivotUp -> solenoid.set(true)
        }
    }

}

class PivotHardware : SubsystemHardware<PivotHardware, PivotComponent>() {
    override val priority: Priority = Priority.Low
    override val period: Time = 10.milli(Second)
    override val syncThreshold: Time = 5.milli(Second)
    override val name: String = "Pivot"

    val solenoidPort by pref(1)
    val solenoid by hardw { Solenoid(solenoidPort) }
}