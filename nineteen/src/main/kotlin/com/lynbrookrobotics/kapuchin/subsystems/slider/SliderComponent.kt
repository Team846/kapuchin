package com.lynbrookrobotics.kapuchin.subsystems.slider

import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.Component
import com.lynbrookrobotics.kapuchin.timing.clock.*
import info.kunalsheth.units.generated.*

class  SliderComponent(hardware: SliderHardware) : Component<SliderComponent, SliderHardware, OffloadedOutput>(hardware, EventLoop) {

    //positive is to the robot's right
    //negative is to the robot's left

    val middle by pref(0, Inch)

    val positionGains by pref {
        val kP by pref(12, Volt, 8, Inch)
        ({ OffloadedPidGains(
                hardware.offloadedSettings.native(kP),
                0.0, 0.0, 0.0
        )})
    }

    override val fallbackController: SliderComponent.(Time) -> OffloadedOutput = {
        PositionOutput(positionGains, hardware.offloadedSettings.native(middle))
    }

    override fun SliderHardware.output(value: OffloadedOutput) = lazyOutput(value)

}