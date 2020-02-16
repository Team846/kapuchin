package com.lynbrookrobotics.kapuchin.subsystems.storage

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.revrobotics.*
import com.revrobotics.CANSparkMaxLowLevel.MotorType.kBrushless
import com.revrobotics.EncoderType.kHallSensor
import edu.wpi.first.wpilibj.DigitalInput
import edu.wpi.first.wpilibj.I2C.Port
import edu.wpi.first.wpilibj.util.Color
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlin.math.roundToInt

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

    override val fallbackController: CarouselComponent.(Time) -> OffloadedOutput = {
        PercentOutput(hardware.escConfig, 0.Percent)
    }

    override fun CarouselHardware.output(value: OffloadedOutput) {
        value.writeTo(esc, pidController)
    }
}