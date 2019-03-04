package com.lynbrookrobotics.kapuchin.choreos

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.collector.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.collector.hookslider.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.collector.pivot.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.handoff.*
import com.lynbrookrobotics.kapuchin.timing.*
import info.kunalsheth.units.generated.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.NonCancellable

suspend fun Subsystems.intakeTeleop() = startChoreo("Intake teleop") {

    val deployCargo by operator.deployCargo.readEagerly().withoutStamps
    val deployPanel by operator.deployPanel.readEagerly().withoutStamps
    val collectCargo by operator.collectCargo.readEagerly().withoutStamps
    val collectPanel by operator.collectPanel.readEagerly().withoutStamps
    val collectGroundPanel by operator.collectGroundPanel.readEagerly().withoutStamps
    val lineTracking by operator.lineTracking.readEagerly().withoutStamps
    val centerSlider by operator.centerSlider.readEagerly().withoutStamps
    val centerCargo by operator.centerCargo.readEagerly().withoutStamps
    val sliderPrecision by operator.sliderPrecision.readEagerly().withoutStamps

    choreography {
        runWhenever(
                { deployCargo } to choreography { deployCargo() },
                { deployPanel } to choreography { deployPanel() },
                { collectCargo } to choreography { collectCargo() },
                { collectPanel } to choreography { collectPanel() },
                { collectGroundPanel } to choreography { collectGroundPanel() },
                { lineTracking } to choreography { trackLine() },
                { centerSlider } to choreography { centerSlider() },
                { centerCargo } to choreography { centerCargo() },
                { !sliderPrecision.isZero } to choreography { collectorSlider?.manualOverride(operator) }
        )
    }
}

suspend fun Subsystems.deployCargo() {
    //Center collector slider
    collectorSlider?.set(0.Inch, electrical)

    //Eject cargo
    collectorRollers?.spin(electrical, collectorRollers.cargoReleaseSpeed)
    freeze()
}

suspend fun Subsystems.deployPanel() = supervisorScope {
    //Eject panel
    val j1 = launch { hookSlider?.set(HookSliderState.Out) }
    delay(0.5.Second)
    val j2 = launch { hook?.set(HookPosition.Down) }

    try {
        freeze()
    } finally {
        withContext(NonCancellable) {
            j1.cancel()
            delay(0.5.Second)
            j2.cancel()
        }
    }
}

suspend fun Subsystems.collectCargo() = supervisorScope {
    //Start rollers
    launch { handoffRollers?.spin(handoffRollers.cargoCollectSpeed) }
    launch { collectorRollers?.spin(electrical, collectorRollers.cargoCollectSpeed) }

    //Center slider
    collectorSlider?.set(0.Inch, electrical)

    //Set handoff pivot down
    withTimeout(1.Second) { handoffPivot?.set(handoffPivot.collectPosition, 10.Degree) }
    val j1 = launch { handoffPivot?.set(handoffPivot.collectPosition, 0.Degree) }

    //lift, collector down
    launch { lift?.set(lift.collectCargo, 0.Inch) }
    launch { collectorPivot?.set(CollectorPivotState.Down) }

    try {
        freeze()
    } finally {
        withContext(NonCancellable) {
            delay(1.Second)
            j1.cancel()
        }
    }
}

suspend fun Subsystems.collectPanel() = coroutineScope {
    //Center slider
    withTimeout(1.Second) { collectorSlider?.set(0.Inch, electrical) }
    launch { collectorSlider?.set(0.Inch, electrical) }

    //Lift down
    withTimeout(1.Second) { lift?.set(lift.collectPanel, 1.Inch) }
    launch { lift?.set(lift.collectPanel, 0.Inch) }

    //Hook down, slider out
    launch { hook?.set(HookPosition.Down) }
    launch { hookSlider?.set(HookSliderState.Out) }
    freeze()
}

suspend fun Subsystems.trackLine() = coroutineScope {
    //Track line with slider
    collectorSlider?.trackLine(0.5.Inch, lineScanner, electrical)
    freeze()
}

suspend fun Subsystems.centerSlider() {
    collectorSlider?.set(
            0.Inch,
            electrical
    )
    freeze()
}

suspend fun Subsystems.centerCargo() {
    collectorRollers?.spin(
            electrical,
            collectorRollers.cargoCenterSpeed, // bottom out
            -collectorRollers.cargoCenterSpeed // top in
    )
    freeze()
}

/**
 * Collect panel from the ground
 *
 * Ends with:
 * CollectorPivot - Up
 * CollectorSlider - Center
 * HandoffPivot - Handoff position
 * VelcroPivot - Down
 * Hook - Up
 * Lift - Collect ground panel height
 */
suspend fun Subsystems.collectGroundPanel() = coroutineScope {
    //Center slider
    collectorSlider?.set(0.Inch, electrical)

    //Lift down
    launch { lift?.set(lift.collectGroundPanel) }

    //Handoff, velcro, hook down
    launch { handoffPivot?.set(handoffPivot.collectPosition) }
    launch { velcroPivot?.set(VelcroPivotPosition.Down) }
    launch { hook?.set(HookPosition.Down) }

    freeze()
}

