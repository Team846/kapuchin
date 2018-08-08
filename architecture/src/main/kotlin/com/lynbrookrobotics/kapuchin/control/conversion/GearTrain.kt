package com.lynbrookrobotics.kapuchin.control.conversion

import info.kunalsheth.units.generated.Quan

data class GearTrain(val inputTeeth: Int, val outputTeeth: Int, val idlers: Int = 0) {
    private val flip = if (idlers % 2 == 0) 1 else -1

    private val inOverOut = inputTeeth.toDouble() / outputTeeth
    private val outOverIn = outputTeeth.toDouble() / inputTeeth

    fun <Q : Quan<Q>> inputToOutput(driveInput: Q) = driveInput * inOverOut * flip
    fun <Q : Quan<Q>> outputToInput(driveOutput: Q) = driveOutput * outOverIn * flip
}