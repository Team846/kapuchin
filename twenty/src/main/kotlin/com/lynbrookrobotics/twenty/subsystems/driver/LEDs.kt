package com.lynbrookrobotics.twenty.subsystems.driver

import com.ctre.phoenix.CANifier
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import java.awt.Color

class LedComponent(hardware: LedHardware) : Component<LedComponent, LedHardware, Color>(hardware, EventLoop) {
    val rainbowPeriod by pref(1, Second)

    val channels by pref {
        val red by pref("LEDChannelA")
        val green by pref("LEDChannelB")
        val blue by pref("LEDChannelC")
        ({
            fun(value: Color) = with(hardware.canifier) {
                setLEDOutput(value.red / 255.0, CANifier.LEDChannel.valueOf(red))
                setLEDOutput(value.green / 255.0, CANifier.LEDChannel.valueOf(green))
                setLEDOutput(value.blue / 255.0, CANifier.LEDChannel.valueOf(blue))
            }
        })
    }

    override val fallbackController: LedComponent.(Time) -> Color = {
        Color(Color.HSBtoRGB(
            ((currentTime.Second / rainbowPeriod.Second % 1.0)).toFloat(),
            1f, 1f
        )) // TODO change to off after testing
    }


    override fun LedHardware.output(value: Color) {
        channels(value)
    }
}

class LedHardware : SubsystemHardware<LedHardware, LedComponent>() {
    override val name = "LEDs"
    override val period = 50.milli(Second)
    override val syncThreshold = 10.milli(Second)
    override val priority = Priority.Low

    private val canifierDeviceId = 60
    val canifier by hardw { CANifier(canifierDeviceId) }
}