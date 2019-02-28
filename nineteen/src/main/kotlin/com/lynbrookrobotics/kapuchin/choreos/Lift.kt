package com.lynbrookrobotics.kapuchin.choreos

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.subsystems.driver.*
import com.lynbrookrobotics.kapuchin.subsystems.lift.*
import info.kunalsheth.units.generated.*

suspend fun Subsystems.liftTeleop() = startChoreo("Lift teleop") {

    val groundHeight by operator.groundHeight.readEagerly().withoutStamps

    val lowPanelHeight by operator.lowPanelHeight.readEagerly().withoutStamps
    val lowCargoHeight by operator.lowCargoHeight.readEagerly().withoutStamps

    val midPanelHeight by operator.midPanelHeight.readEagerly().withoutStamps
    val midCargoHeight by operator.midCargoHeight.readEagerly().withoutStamps

    val highPanelHeight by operator.highPanelHeight.readEagerly().withoutStamps
    val highCargoHeight by operator.highCargoHeight.readEagerly().withoutStamps

    choreography {
        whenever({ groundHeight || lowPanelHeight || lowCargoHeight || midPanelHeight || midCargoHeight || highPanelHeight || highCargoHeight }) {
            runWhile({ groundHeight }) {
                lift?.set(lift.groundHeight, 0.Inch)
            }
            runWhile({ lowPanelHeight }) {
                lift?.set(lift.panelLowRocket, 0.Inch)
            }
            runWhile({ lowCargoHeight }) {
                lift?.set(lift.cargoLowRocket, 0.Inch)
            }
            runWhile({ midPanelHeight }) {
                lift?.set(lift.panelMidRocket, 0.Inch)
            }
            runWhile({ midCargoHeight }) {
                lift?.set(lift.cargoMidRocket, 0.Inch)
            }
            runWhile({ highPanelHeight }) {
                lift?.set(lift.panelHighRocket, 0.Inch)
            }
            runWhile({ highCargoHeight }) {
                lift?.set(lift.cargoHighRocket, 0.Inch)
            }
        }
    }
}
