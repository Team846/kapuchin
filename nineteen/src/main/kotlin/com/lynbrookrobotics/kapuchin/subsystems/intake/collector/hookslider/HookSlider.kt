package com.lynbrookrobotics.kapuchin.subsystems.intake.collector.hookslider

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.collector.hookslider.HookSliderState.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.collector.pivot.*
import com.lynbrookrobotics.kapuchin.timing.*
import edu.wpi.first.wpilibj.Solenoid
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class HookSliderComponent(hardware: HookSliderHardware) : Component<HookSliderComponent, HookSliderHardware, HookSliderState>(hardware, Subsystems.pneumaticTicker) {

    override val fallbackController: HookSliderComponent.(Time) -> HookSliderState = { In }

    override fun HookSliderHardware.output(value: HookSliderState) {
        val subsystems = Subsystems.instance!!

        val liftPos = subsystems.lift?.hardware?.position?.optimizedRead(currentTime, 0.Second)?.y ?: 0.Inch

        if (liftPos > 6.Inch && subsystems.collectorPivot?.hardware?.solenoid?.get() == CollectorPivotState.Up.output) {
            solenoid.set(value.output)
        }
    }
}

class HookSliderHardware : SubsystemHardware<HookSliderHardware, HookSliderComponent>() {
    override val priority: Priority = Priority.Low
    override val period: Time = 100.milli(Second)
    override val syncThreshold: Time = 20.milli(Second)
    override val name: String = "Hook Slider"

    val solenoidPort = 1
    val solenoid = Solenoid(solenoidPort)
}
