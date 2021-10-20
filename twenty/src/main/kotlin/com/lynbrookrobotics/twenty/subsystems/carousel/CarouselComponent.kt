package com.lynbrookrobotics.twenty.subsystems.carousel

import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.twenty.Subsystems
import info.kunalsheth.units.generated.*

class CarouselComponent(hardware: CarouselHardware) :
    Component<CarouselComponent, CarouselHardware, OffloadedOutput>(hardware) {

    val positionGains by pref {
        val kP by pref(12, Volt, 90, Degree)
        val kI by pref(4, Volt, 90, DegreeSecond)
        val kD by pref(0, Volt, 60, DegreePerSecond)
        ({
            OffloadedEscGains(
                kP = hardware.conversions.encoder.native(kP),
                kI = hardware.conversions.encoder.native(kI),
                kD = hardware.conversions.encoder.native(kD)
            )
        })
    }

    val shootFastSpeed by pref(-40, Percent)
    val shootSlowSpeed by pref(-20, Percent)
    val zeroSpeed by pref(30, Percent)

    val state = CarouselState(this)

    override val fallbackController: CarouselComponent.(Time) -> OffloadedOutput = {
        PercentOutput(hardware.escConfig, 0.Percent)
    }

    private val ammoGraph = graph("ammo", Each)

    override fun CarouselHardware.output(value: OffloadedOutput) {
        value.writeTo(esc, pidController)

        ammoGraph(currentTime, state.balls.Each)
    }

    init {
        Subsystems.uiTicker.runOnTick { time ->
            ammoGraph(time, state.balls.Each)
        }
    }
}