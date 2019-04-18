package com.lynbrookrobotics.kapuchin.choreos

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.subsystems.collector.*
import com.lynbrookrobotics.kapuchin.subsystems.collector.hookslider.*
import com.lynbrookrobotics.kapuchin.subsystems.collector.pivot.*
import com.lynbrookrobotics.kapuchin.timing.*
import info.kunalsheth.units.generated.*
import kotlinx.coroutines.*

suspend fun Subsystems.intakeTeleop() = startChoreo("Intake teleop") {

    val deployCargo by operator.deployCargo.readEagerly().withoutStamps
    val softDeployCargo by operator.softDeployCargo.readEagerly().withoutStamps
    val deployPanel by operator.deployPanel.readEagerly().withoutStamps

    val collectCargo by driver.collectCargo.readEagerly().withoutStamps
    val lilDicky by operator.lilDicky.readEagerly().withoutStamps

    val operatorLineTracking by operator.lineTracking.readEagerly().withoutStamps
    val centerSlider by operator.centerSlider.readEagerly().withoutStamps
    val centerCargoLeft by operator.centerCargoLeft.readEagerly().withoutStamps
    val centerCargoRight by operator.centerCargoRight.readEagerly().withoutStamps

    val pivotDown by operator.pivotDown.readEagerly().withoutStamps
    val sliderPrecision by operator.sliderPrecision.readEagerly().withoutStamps

    choreography {
        runWhenever(
                { deployCargo || softDeployCargo } to choreography { deployCargo(softDeployCargo) },
                { deployPanel } to choreography { deployPanel() },
                { collectCargo } to choreography { collectCargo() },
                { operatorLineTracking } to choreography { collectorSlider?.trackLine(lineScanner, electrical) },
                { lilDicky } to choreography { lilDicky() },

                { centerSlider } to choreography { centerSlider(0.Inch) },
                { centerCargoLeft } to choreography { centerCargo(true) },
                { centerCargoRight } to choreography { centerCargo(false) },

                { pivotDown } to choreography { pivotDown() },
                { !sliderPrecision.isZero } to choreography { collectorSlider?.manualOverride(operator) }
        )
    }
}

suspend fun Subsystems.deployCargo(soft: Boolean) {
    collectorRollers?.spin(electrical, if (soft) collectorRollers.cargoReleaseSpeed / 2 else collectorRollers.cargoReleaseSpeed)
}

suspend fun Subsystems.pivotDown() {
    collectorPivot?.set(CollectorPivotState.Down)
    freeze()
}

suspend fun Subsystems.deployPanel() = supervisorScope {
    //Eject panel

    var hookSliderOut: Job? = null
    var hookDown: Job? = null
    try {
        hookSliderOut = scope.launch { hookSlider?.set(HookSliderState.Out) }
        delay(0.2.Second)
        hookDown = scope.launch { hook?.set(HookPosition.Down) }

        scope.launch {
            withTimeout(1.5.Second) { rumble.set(TwoSided(100.Percent, 0.Percent)) }
        }

        withContext(NonCancellable) { delay(0.2.Second) }

        freeze()
    } finally {
        withContext(NonCancellable) {
            hookSliderOut?.cancel()
            delay(0.5.Second)
            hookDown?.cancel()
        }
    }
}

suspend fun Subsystems.collectCargo() = supervisorScope {

    try {
        lift?.set(lift.cargoCollect, 2.Inch)
        launch { lift?.set(lift.cargoCollect, 0.Inch) }

        launch { collectorPivot?.set(CollectorPivotState.Down) }

        //Start rollers
        launch { collectorRollers?.spin(electrical, collectorRollers.cargoCollectSpeed) }

        //Center slider
        collectorSlider?.set(0.Inch, electrical)

        freeze()
    } finally {
        withContext(NonCancellable) {
            withTimeout(1.Second) {
                collectorRollers?.spin(electrical, 8.Volt)
            }
        }
    }
}

suspend fun Subsystems.lilDicky() = coroutineScope {
    var hookDown: Job? = null
    try {
        hookDown = scope.launch { hook?.set(HookPosition.Down) }
        lift?.set(lift.panelCollect, 0.Inch)
        freeze()
    } finally {
        withContext(NonCancellable) {
            scope.launch { lift?.set(lift.panelCollectStroke, 0.Inch) }
            val hookSliderOut = scope.launch { hookSlider?.set(HookSliderState.Out) }
            delay(0.2.Second)
            hookDown?.cancel()
            delay(0.2.Second)
            hookSliderOut.cancel()
            scope.launch {
                withTimeout(1.5.Second) {
                    rumble.set(TwoSided(100.Percent, 0.Percent))
                }
            }
        }
    }
}

suspend fun Subsystems.centerSlider(tolerance: Length = 1.Inch) {
    collectorSlider?.set(
            0.Inch,
            electrical,
            tolerance
    )
}

suspend fun Subsystems.centerCargo(flip: Boolean) {
    collectorRollers?.spin(
            electrical,
            top = if (flip) -5.Volt else 11.5.Volt,
            bottom = if (flip) 11.5.Volt else -5.Volt
    )
}
