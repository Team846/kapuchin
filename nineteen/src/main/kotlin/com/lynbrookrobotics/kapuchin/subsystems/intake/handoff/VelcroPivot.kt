package com.lynbrookrobotics.kapuchin.subsystems.intake.handoff

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.handoff.VelcroPivotPosition.*
import com.lynbrookrobotics.kapuchin.timing.*
import edu.wpi.first.wpilibj.Solenoid
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

enum class VelcroPivotPosition(val output: Boolean) {
    Up(false), Down(true)
}

class VelcroPivotComponent(hardware: VelcroPivotHardware) : Component<VelcroPivotComponent, VelcroPivotHardware, VelcroPivotPosition>(hardware, Subsystems.pneumaticTicker) {
    override val fallbackController: VelcroPivotComponent.(Time) -> VelcroPivotPosition = { Up }
    override fun VelcroPivotHardware.output(value: VelcroPivotPosition) {
        solenoid.set(value.output)
    }
}

class VelcroPivotHardware : SubsystemHardware<VelcroPivotHardware, VelcroPivotComponent>() {
    override val priority: Priority = Priority.Low
    override val period: Time = 100.milli(Second)
    override val syncThreshold: Time = 20.milli(Second)
    override val name: String = "Velcro Pivot"

    val solenoidPort by pref(1)
    val solenoid = Solenoid(solenoidPort)

}
