package com.lynbrookrobotics.kapuchin.subsystems.slider

import com.lynbrookrobotics.kapuchin.hardware.offloaded.OffloadedOutput
import com.lynbrookrobotics.kapuchin.hardware.offloaded.PercentOutput
import com.lynbrookrobotics.kapuchin.subsystems.Component
import info.kunalsheth.units.generated.Percent
import info.kunalsheth.units.generated.Time

class  SliderComponent(hardware: SliderHardware) : Component<SliderComponent, SliderHardware, OffloadedOutput>(hardware) {

    override val fallbackController: SliderComponent.(Time) -> OffloadedOutput = { PercentOutput(0.Percent) }

    override fun SliderHardware.output(value: OffloadedOutput) = lazyOutput(value)

}