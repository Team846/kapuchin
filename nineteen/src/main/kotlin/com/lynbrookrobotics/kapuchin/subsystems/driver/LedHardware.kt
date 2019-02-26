package com.lynbrookrobotics.kapuchin.subsystems.driver

import com.ctre.phoenix.CANifier
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import java.awt.Color

class LedHardware : RobotHardware<LedHardware>() {
    override val priority: Priority = Priority.Low
    override val name: String = "LED Lights"

    val canifierDeviceID by pref(60)
    val canifier by hardw { CANifier(canifierDeviceID) }

    val channels by pref {
        val red by pref("LEDChannelA")
        val green by pref("LEDChannelB")
        val blue by pref("LEDChannelC")
        ({
            fun(value: Color) {
                canifier.setLEDOutput(value.red / 255.0, CANifier.LEDChannel.valueOf(red))
                canifier.setLEDOutput(value.green / 255.0, CANifier.LEDChannel.valueOf(green))
                canifier.setLEDOutput(value.blue / 255.0, CANifier.LEDChannel.valueOf(blue))
            }
        })
    }
}