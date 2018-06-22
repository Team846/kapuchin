package com.lynbrookrobotics.kapuchin.control.conversion

import info.kunalsheth.units.generated.*

data class GearTrain(val inputTeeth: Int, val outputTeeth: Int, val idlers: Int = 0) {
    private val flip = if (idlers % 2 == 0) 1 else -1

    private val inOverOut = inputTeeth.toDouble() / outputTeeth
    private val outOverIn = outputTeeth.toDouble() / inputTeeth

    fun inputToOutput(driveInput: Angle): Angle = driveInput * inOverOut * flip

    fun inputToOutput(driveInput: AngularVelocity) =
            inputToOutput(driveInput * t) / t

    fun inputToOutput(driveInput: AngularAcceleration) =
            inputToOutput(driveInput * t) / t

    fun outputToInput(driveOutput: Angle): Angle = driveOutput * outOverIn * flip

    fun outputToInput(driveOutput: AngularVelocity) =
            outputToInput(driveOutput * t) / t

    fun outputToInput(driveOutput: AngularAcceleration) =
            outputToInput(driveOutput * t) / t

    companion object {
        private val t = 1.Second
    }
}