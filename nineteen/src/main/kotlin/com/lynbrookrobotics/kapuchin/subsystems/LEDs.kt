package com.lynbrookrobotics.kapuchin.subsystems

import com.ctre.phoenix.CANifier
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import java.awt.Color
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

class LedComponent(hardware: LedHardware) : Component<LedComponent, LedHardware, Color>(hardware, EventLoop) {

    private val hue = Color.RGBtoHSB(255, 0, 0, null)[0]

    override val fallbackController: LedComponent.(Time) -> Color = {
        Color(Color.HSBtoRGB(
                hue,
                1.0f,
                (abs(sin(((currentTime.Second / periods.second.Second) % 1.0) * PI))).toFloat()
        ))
    }

    override fun LedHardware.output(value: Color) {
        channels(value)
    }

    val periods by pref {
        val rainbow by pref(1, Second)
        val fade by pref(3, Second)
        ({ rainbow to fade })
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
    override val period = 50.milli(Second)
    override val syncThreshold = 10.milli(Second)
    override val priority = Priority.Low

    val canifierDeviceId = 60
    val canifier by hardw { CANifier(canifierDeviceId) }
}