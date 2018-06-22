package com.lynbrookrobotics.kapuchin.hardware

import com.ctre.phoenix.ErrorCode
import com.ctre.phoenix.ErrorCode.OK
import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.ControlMode.*
import com.ctre.phoenix.motorcontrol.can.TalonSRX
import com.lynbrookrobotics.kapuchin.hardware.offloaded.LazyOffloadedOutputWriter
import com.lynbrookrobotics.kapuchin.logging.Level.Error
import com.lynbrookrobotics.kapuchin.logging.log
import com.lynbrookrobotics.kapuchin.subsystems.Named
import info.kunalsheth.units.generated.*

fun Named.lazyOutput(talonSRX: TalonSRX, idx: Int = 0, timeout: Time): LazyOffloadedOutputWriter {
    fun wrap(f: (Int, Double, Int) -> ErrorCode): (Double) -> Unit = {
        val err = f(idx, it, timeout.milli(T::Second).toInt())
        if (err != OK) log(Error) { "TalonSRX returned error code $err" }
    }

    return LazyOffloadedOutputWriter(
            writeKp = wrap(talonSRX::config_kP),
            writeKi = wrap(talonSRX::config_kI),
            writeKd = wrap(talonSRX::config_kD),
            writeKf = wrap(talonSRX::config_kF),
            writePercent = { talonSRX.set(PercentOutput, it.Tick) },
            writeCurrent = { talonSRX.set(Current, it.Ampere) },
            writePosition = { talonSRX.set(Position, it) },
            writeVelocity = { talonSRX.set(ControlMode.Velocity, it) }
    )
}