package com.lynbrookrobotics.kapuchin.subsystems

import com.ctre.phoenix.CANifier
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.timing.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import java.awt.Color

class LedComponent(hardware: LedHardware) : Component<LedComponent, LedHardware, Color>(hardware) {

    override val fallbackController: LedComponent.(Time) -> Color = {
        Color(Color.HSBtoRGB(((currentTime.Second / 3 % 1.0)).toFloat(), 1f, 1f))
    }

    override fun LedHardware.output(value: Color) {
        channels(value)
    }

    val channels by pref {
        val red by pref("LEDChannelA")
        val green by pref("LEDChannelB")
        val blue by pref("LEDChannelC")
        ({
            fun(value: Color) {
                hardware.canifier.setLEDOutput(value.red / 255.0, CANifier.LEDChannel.valueOf(red))
                hardware.canifier.setLEDOutput(value.green / 255.0, CANifier.LEDChannel.valueOf(green))
                hardware.canifier.setLEDOutput(value.blue / 255.0, CANifier.LEDChannel.valueOf(blue))
            }
        })
    }

}

class LedHardware : SubsystemHardware<LedHardware, LedComponent>() {
    override val name = "LEDs"
    override val period = 100.milli(Second)
    override val syncThreshold = 10.milli(Second)
    override val priority = Priority.Low

    val canifierDeviceId = 60
    val canifier by hardw { CANifier(canifierDeviceId) }
}