package com.lynbrookrobotics.kapuchin.hardware

import edu.wpi.first.wpilibj.CounterBase.EncodingType.k1X
import edu.wpi.first.wpilibj.Encoder
import info.kunalsheth.units.generated.Frequency
import info.kunalsheth.units.generated.Hertz
import info.kunalsheth.units.generated.Second
import info.kunalsheth.units.generated.Time

class TimeStampedEncoder(a: Int, b: Int) : Encoder(a, b, false, k1X) {
    val timeStamp get() = m_aSource.readRisingTimestamp().Second
    val period = super.getPeriod().Second
    val rate = super.getRate().Hertz
    val ticks = super.get().toDouble()
}