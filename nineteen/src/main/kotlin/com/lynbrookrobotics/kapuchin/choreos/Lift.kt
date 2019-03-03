package com.lynbrookrobotics.kapuchin.choreos

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.routines.*
import info.kunalsheth.units.generated.*

suspend fun Subsystems.liftTeleop() = startChoreo("Lift teleop") {

    val lowPanelHeight by operator.lowPanelHeight.readEagerly().withoutStamps
    val lowCargoHeight by operator.lowCargoHeight.readEagerly().withoutStamps

    val midPanelHeight by operator.midPanelHeight.readEagerly().withoutStamps
    val midCargoHeight by operator.midCargoHeight.readEagerly().withoutStamps

    val highPanelHeight by operator.highPanelHeight.readEagerly().withoutStamps
    val highCargoHeight by operator.highCargoHeight.readEagerly().withoutStamps

    val liftPrecision by operator.liftPrecision.readEagerly().withoutStamps

    choreography {
        runWhenever(
            { lowPanelHeight } to choreography { lift?.set(lift.panelLowRocket, 0.Inch) },
            { lowCargoHeight } to choreography { lift?.set(lift.cargoLowRocket, 0.Inch) },
            { midPanelHeight } to choreography { lift?.set(lift.panelMidRocket, 0.Inch) },
            { midCargoHeight } to choreography { lift?.set(lift.cargoMidRocket, 0.Inch) },
            { highPanelHeight } to choreography { lift?.set(lift.panelHighRocket, 0.Inch) },
            { highCargoHeight } to choreography { lift?.set(lift.cargoHighRocket, 0.Inch) },
            { !liftPrecision.isZero } to choreography { lift?.manualOverride(operator) }
        )
    }
}
