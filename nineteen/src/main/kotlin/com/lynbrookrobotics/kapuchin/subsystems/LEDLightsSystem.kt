package com.lynbrookrobotics.kapuchin.subsystems

import com.ctre.phoenix.CANifier
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import java.awt.Color

class LEDLightsComponent(hardware: LEDLightsHardware) : Component<LEDLightsComponent, LEDLightsHardware, Color>(hardware, EventLoop) {

    private val channelMap: HashMap<String, CANifier.LEDChannel> = hashMapOf(
            "A" to CANifier.LEDChannel.LEDChannelA,
            "B" to CANifier.LEDChannel.LEDChannelB,
            "C" to CANifier.LEDChannel.LEDChannelC)

    val ledChannels by pref {
        val red by pref("A")
        val green by pref("B")
        val blue by pref("C")
        ({
            fun(value: Color) {
                hardware.canifier.setLEDOutput(value.red / 255.0, channelMap[red])
                hardware.canifier.setLEDOutput(value.green / 255.0, channelMap[green])
                hardware.canifier.setLEDOutput(value.blue / 255.0, channelMap[blue])
            }
        })
    }

    override val fallbackController: LEDLightsComponent.(Time) -> Color = {
        Color(Color.HSBtoRGB(((currentTime.Second / 3 % 1.0)).toFloat(), 1f, 1f))
    }

    override fun LEDLightsHardware.output(value: Color) {
        ledChannels(value)
    }
}

class LEDLightsHardware : SubsystemHardware<LEDLightsHardware, LEDLightsComponent>() {
    override val priority: Priority = Priority.Low
    override val period: Time = 100.milli(Second)
    override val syncThreshold: Time = 10.milli(Second)
    override val name: String = "LED Lights"

    val canifierDeviceID by pref(50)
    val canifier by hardw { CANifier(canifierDeviceID) }
}