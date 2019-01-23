package com.lynbrookrobotics.kapuchin.subsystems.drivetrain

import com.lynbrookrobotics.kapuchin.control.data.stampWith
import com.lynbrookrobotics.kapuchin.hardware.HardwareInit.Companion.hardw
import com.lynbrookrobotics.kapuchin.hardware.LineScanner
import com.lynbrookrobotics.kapuchin.hardware.Sensor.Companion.sensor
import com.lynbrookrobotics.kapuchin.hardware.Sensor.Companion.with
import com.lynbrookrobotics.kapuchin.logging.Grapher.Companion.graph
import com.lynbrookrobotics.kapuchin.preferences.pref
import com.lynbrookrobotics.kapuchin.subsystems.SubsystemHardware
import com.lynbrookrobotics.kapuchin.timing.Priority
import com.lynbrookrobotics.kapuchin.timing.clock.EventLoop
import edu.wpi.first.wpilibj.SerialPort
import info.kunalsheth.units.generated.Foot
import info.kunalsheth.units.generated.Inch
import info.kunalsheth.units.generated.Second
import info.kunalsheth.units.math.milli

class LineScannerHardware : SubsystemHardware<LineScannerHardware, Nothing>() {
    override val priority = Priority.Medium
    override val period = 15.milli(Second)
    override val syncThreshold = 10.milli(Second)
    override val name = "Line Scanner"

    private val lineScannerPort by pref("kUSB2")
    private val lineScanner by hardw { LineScanner(SerialPort.Port.valueOf(lineScannerPort)) }
    private val exposure by pref(200)
    private val triggerLevel by pref(100)
    private val scanWidth by pref(12, Inch)

    val linePosition = sensor(lineScanner) {
        val data = lineScanner(exposure.toUByte())

        val max = data.max()!!
        val peakLocation = if (max > triggerLevel) (
                sequenceOf(0.toByte()) +
                        data.asSequence() +
                        0.toByte()
                )
                .mapIndexed { i, b -> i to b }
                .filter { (_, b) -> b >= max }
                .map { (i, _) -> i }
                .average() - resolution / 2
        else null

        (if (peakLocation != null)
            scanWidth * peakLocation / lineScanner.resolution
        else null) stampWith it
    }
}