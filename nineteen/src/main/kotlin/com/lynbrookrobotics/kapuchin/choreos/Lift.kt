package com.lynbrookrobotics.kapuchin.choreos

import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.subsystems.driver.*
import com.lynbrookrobotics.kapuchin.subsystems.lift.*
import info.kunalsheth.units.generated.*

suspend fun liftTeleop(
        lift: LiftComponent,
        oper: OperatorHardware
) = startChoreo("Lift teleop") {

    val groundHeight by oper.groundHeight.readEagerly().withoutStamps

    val lowPanelHeight by oper.lowPanelHeight.readEagerly().withoutStamps
    val lowCargoHeight by oper.lowCargoHeight.readEagerly().withoutStamps

    val midPanelHeight by oper.midPanelHeight.readEagerly().withoutStamps
    val midCargoHeight by oper.midCargoHeight.readEagerly().withoutStamps

    val highPanelHeight by oper.highPanelHeight.readEagerly().withoutStamps
    val highCargoHeight by oper.highCargoHeight.readEagerly().withoutStamps

    choreography {
        whenever({ groundHeight || lowPanelHeight || lowCargoHeight || midPanelHeight || midCargoHeight || highPanelHeight || highCargoHeight }) {
            runWhile({ groundHeight }) {
                lift.set(lift.groundHeight, 0.Inch)
            }
            runWhile({ lowPanelHeight }) {
                lift.set(lift.panelLowRocket, 0.Inch)
            }
            runWhile({ lowCargoHeight }) {
                lift.set(lift.cargoLowRocket, 0.Inch)
            }
            runWhile({ midPanelHeight }) {
                lift.set(lift.panelMidRocket, 0.Inch)
            }
            runWhile({ midCargoHeight }) {
                lift.set(lift.cargoMidRocket, 0.Inch)
            }
            runWhile({ highPanelHeight }) {
                lift.set(lift.panelHighRocket, 0.Inch)
            }
            runWhile({ highCargoHeight }) {
                lift.set(lift.cargoHighRocket, 0.Inch)
            }
        }
    }
}
