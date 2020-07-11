package com.lynbrookrobotics.kapuchin.control.conversion

import info.kunalsheth.units.generated.*

/**
 * Gear train conversion utility
 *
 * Utility functions for calculating input and output angles for simple gear trains
 *
 * @author Kunal
 * @see OffloadedNativeConversion
 * @see EncoderConversion
 *
 * @param inputTeeth must be greater than zero
 * @param outputTeeth must be greater than zero
 * @param idlers must be greater than or equal to zero
 *
 * @property inputTeeth number of teeth on the input gear
 * @property outputTeeth number of teeth on the output gear
 * @property idlers number of gears between the input and output gears
 */
data class GearTrain(val inputTeeth: Int, val outputTeeth: Int, val idlers: Int = 1) {
    private val flip = if (idlers % 2 == 1) 1 else -1

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
}