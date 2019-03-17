package com.lynbrookrobotics.kapuchin.choreos

import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.subsystems.driver.*
import com.lynbrookrobotics.kapuchin.subsystems.lift.*
import info.kunalsheth.units.generated.*

suspend fun LiftComponent.teleop(operator: OperatorHardware, driver: DriverHardware) = startChoreo("Lift teleop") {

    val liftDown by driver.liftDown.readEagerly().withoutStamps

    val lowPanelHeight by operator.lowPanelHeight.readEagerly().withoutStamps
    val lowCargoHeight by operator.lowCargoHeight.readEagerly().withoutStamps

    val midPanelHeight by operator.midPanelHeight.readEagerly().withoutStamps
    val midCargoHeight by operator.midCargoHeight.readEagerly().withoutStamps
    val cargoShipCargoHeight by operator.cargoShipCargoHeight.readEagerly().withoutStamps

    val highPanelHeight by operator.highPanelHeight.readEagerly().withoutStamps
    val highCargoHeight by operator.highCargoHeight.readEagerly().withoutStamps

    val liftPrecision by operator.liftPrecision.readEagerly().withoutStamps

    val currentHeight by hardware.position.readEagerly().withoutStamps

    choreography {
        launchWhenever(
                { liftDown } to choreography { set(collectCargo, 0.Inch) },
                { lowPanelHeight } to choreography { set(panelLowRocket, 0.Inch) },
                { lowCargoHeight } to choreography { set(cargoLowRocket, 0.Inch) },
                { midPanelHeight } to choreography { set(panelMidRocket, 0.Inch) },
                { midCargoHeight } to choreography { set(cargoMidRocket, 0.Inch) },
                { cargoShipCargoHeight } to choreography { set(cargoCargoShip, 0.Inch) },
                { highPanelHeight } to choreography { set(panelHighRocket, 0.Inch) },
                { highCargoHeight } to choreography { set(cargoHighRocket, 0.Inch) },

                { !liftPrecision.isZero } to choreography { manualOverride(operator) }
        )
    }
}
