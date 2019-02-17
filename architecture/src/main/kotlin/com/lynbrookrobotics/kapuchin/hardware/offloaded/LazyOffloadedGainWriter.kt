package com.lynbrookrobotics.kapuchin.hardware.offloaded

import com.lynbrookrobotics.kapuchin.control.conversion.*
import info.kunalsheth.units.generated.*

/**
 * CAN electronic speed controller output utility
 *
 * Utility class for minimizing CAN bus congestion when interfacing with ESCs.
 * Intended for TalonSRXs. Please look at the TalonSRX software manual for more information.
 *
 * @author Kunal
 * @see OffloadedNativeConversion
 * @see OffloadedOutput
 *
 * @param writeKp function to configure ESC proportional gain
 * @param writeKi function to configure ESC integral gain
 * @param writeKd function to configure ESC derivative gain
 * @param writeKf function to configure ESC feed forward term
 * @param writeVelocity function to set offloaded control loop velocity target
 * @param writePosition function to set offloaded control loop position target
 * @param writePercent function to set ESC percent output
 * @param writeCurrent function to set ESC current output
 */
fun lazyOffloadedGainWriter(
        writeKp: (Double) -> Unit,
        writeKi: (Double) -> Unit,
        writeKd: (Double) -> Unit,
        writeKf: (Double) -> Unit,
        writeVelocity: (Double) -> Unit,
        writePosition: (Double) -> Unit,
        writePercent: (Dimensionless) -> Unit,
        writeCurrent: (I) -> Unit
): (OffloadedOutput) -> Unit {

    var current: OffloadedPidGains? = null

    return fun(output: OffloadedOutput) {
        if (output is OffloadedPidControlLoop) {
            val (newKp, newKi, newKd, newKf) = output.gains
            if (newKp != current?.kP) writeKp(newKp)
            if (newKi != current?.kI) writeKi(newKi)
            if (newKd != current?.kD) writeKd(newKd)
            if (newKf != current?.kF) writeKf(newKf)
            current = output.gains
        }

        when (output) {
            is VelocityOutput -> writeVelocity(output.output)
            is PositionOutput -> writePosition(output.output)
            is PercentOutput -> writePercent(output.output)
            is CurrentOutput -> writeCurrent(output.output)
        }
    }
}