package com.lynbrookrobotics.kapuchin.choreos

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.routines.*
import info.kunalsheth.units.generated.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

suspend fun Subsystems.liftTeleop() = lift?.run {
    startChoreo("Lift teleop") {

        val liftDown by driver.liftDown.readEagerly().withoutStamps

        val cargoShipHeight by operator.cargoShipHeight.readEagerly().withoutStamps
        val rocketLowHeight by operator.rocketLowHeight.readEagerly().withoutStamps
        val rocketMidHeight by operator.rocketMidHeight.readEagerly().withoutStamps
        val rocketHighHeight by operator.rocketHighHeight.readEagerly().withoutStamps

        val liftPrecision by operator.liftPrecision.readEagerly().withoutStamps

        choreography {
            launchWhenever(
                    { liftDown } to choreography { liftDown() },

                    { cargoShipHeight } to choreography { set(cargoShip, 0.Inch) },
                    { rocketLowHeight } to choreography { set(rocketLow(operator.currentPiece), 0.Inch) },
                    { rocketMidHeight } to choreography { set(rocketMid(operator.currentPiece), 0.Inch) },
                    { rocketHighHeight } to choreography { set(rocketHigh(operator.currentPiece), 0.Inch) },

                    { !liftPrecision.isZero } to choreography { manualOverride(operator) }
            )
        }
    }
}

suspend fun Subsystems.liftDown() = coroutineScope {
    withTimeout(0.5.Second) {
        collectorSlider?.set(
                0.Inch,
                electrical,
                2.5.Inch
        )
    }
    launch { centerSlider() }
    lift?.run { set(cargoCollect, 1.Inch) }
}