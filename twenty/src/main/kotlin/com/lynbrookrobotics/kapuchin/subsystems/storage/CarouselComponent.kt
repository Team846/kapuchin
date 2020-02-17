package com.lynbrookrobotics.kapuchin.subsystems.storage

import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import info.kunalsheth.units.generated.*

class CarouselComponent(hardware: CarouselHardware) : Component<CarouselComponent, CarouselHardware, Pair<OffloadedOutput, CarouselMagazineState>>(hardware) {

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

    private var magazineState = CarouselMagazineState.empty
    override val fallbackController: CarouselComponent.(Time) -> Pair<OffloadedOutput, CarouselMagazineState> = {
        PercentOutput(hardware.escConfig, 0.Percent) to magazineState
    }

    private val ammoGraph = graph("Ammo", Each)
    override fun CarouselHardware.output(value: Pair<OffloadedOutput,CarouselMagazineState>) {
        value.first.writeTo(esc, pidController)
        magazineState = value.second
        ammoGraph(currentTime, magazineState.fullSlots.Each)
    }
}