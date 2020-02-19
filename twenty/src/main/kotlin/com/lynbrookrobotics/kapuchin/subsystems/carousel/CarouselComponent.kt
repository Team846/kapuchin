package com.lynbrookrobotics.kapuchin.subsystems.carousel

import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import info.kunalsheth.units.generated.*

class CarouselComponent(hardware: CarouselHardware) : Component<CarouselComponent, CarouselHardware, OffloadedOutput>(hardware) {

    val positionGains by pref {
        val kP by pref(12, Volt, 90, Degree)
        val kD by pref(0, Volt, 60, DegreePerSecond)
        ({
            OffloadedEscGains(
                    syncThreshold = hardware.syncThreshold,
                    kP = hardware.conversions.encoder.native(kP),
                    kD = hardware.conversions.encoder.native(kD)
            )
        })
    }

    val collectSlot by pref(0, CarouselSlot)
    val sensorSlot by pref(0, CarouselSlot)
    val shootSlot by pref(0.5, CarouselSlot)

    val state = CarouselState(this)

    override val fallbackController: CarouselComponent.(Time) -> OffloadedOutput = {
        PercentOutput(hardware.escConfig, 0.Percent)
    }

    private val ammoGraph = graph("Ammo", Each)
    override fun CarouselHardware.output(value: OffloadedOutput) {
        value.writeTo(esc, pidController)
        ammoGraph(currentTime, state.ammo.Each)
    }
}