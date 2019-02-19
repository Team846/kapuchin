package com.lynbrookrobotics.kapuchin.routines

suspend fun CollectorSliderComponent.trackLine(lineScanner: LineScannerHardware) = startRoutine("Track line") {

    val target by lineScanner.linePosition.readOnTick.withoutStamps
    val current by hardware.position.readOnTick.withoutStamps
    val vBat by electrical.batteryVoltage.readEagerly.withoutStamps

    controller {
        val error = target - current
        val voltage = kP * error

        voltageToDutyCycle(voltage, vBat).takeIf {
            current in target `±` tolerance
        }
    }
}
