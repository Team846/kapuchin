package com.lynbrookrobotics.nineteen.subsystems.driver

import com.ctre.phoenix.CANifier
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import edu.wpi.first.wpilibj.DriverStation.Alliance
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import java.awt.Color
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

class LedComponent(hardware: LedHardware) : Component<LedComponent, LedHardware, Color>(hardware, EventLoop) {

    override val fallbackController: LedComponent.(Time) -> Color = {
        when (hardware.driver.station.alliance) {
            Alliance.Blue -> Color(Color.HSBtoRGB(
                    2 / 3f, 1.0f,
                    (abs(sin(((currentTime.Second / periods.second.Second) % 1.0) * PI))).toFloat()
            ))
            Alliance.Red -> Color(Color.HSBtoRGB(
                    0 / 3f, 1.0f,
                    (abs(sin(((currentTime.Second / periods.second.Second) % 1.0) * PI))).toFloat()
            ))
            else -> Color(Color.HSBtoRGB(
                    ((currentTime.Second / periods.first.Second % 1.0)).toFloat(),
                    1f, 1f
            ))
        }
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
            fun(value: Color) = with(hardware.canifier) {
                setLEDOutput(value.red / 255.0, CANifier.LEDChannel.valueOf(red))
                setLEDOutput(value.green / 255.0, CANifier.LEDChannel.valueOf(green))
                setLEDOutput(value.blue / 255.0, CANifier.LEDChannel.valueOf(blue))
            }
        })
    }

}

class LedHardware(
        val driver: DriverHardware
) : SubsystemHardware<LedHardware, LedComponent>() {
    override val name = "LEDs"
    override val period = 50.milli(Second)
    override val syncThreshold = 10.milli(Second)
    override val priority = Priority.Low

    val canifierDeviceId = 60
    val canifier by hardw { CANifier(canifierDeviceId) }
}