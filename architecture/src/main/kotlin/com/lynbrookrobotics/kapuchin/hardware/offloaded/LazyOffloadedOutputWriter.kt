package com.lynbrookrobotics.kapuchin.hardware.offloaded

import info.kunalsheth.units.generated.Ampere
import info.kunalsheth.units.generated.Dimensionless

class LazyOffloadedOutputWriter(
        private val writeKp: (Double) -> Unit,
        private val writeKi: (Double) -> Unit,
        private val writeKd: (Double) -> Unit,
        private val writeKf: (Double) -> Unit,
        private val writeVelocity: (Double) -> Unit,
        private val writePosition: (Double) -> Unit,
        private val writePercent: (Dimensionless) -> Unit,
        private val writeCurrent: (Ampere) -> Unit
) : (OffloadedOutput) -> Unit {

    private var current: OffloadedPidGains? = null

    override fun invoke(output: OffloadedOutput) {
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