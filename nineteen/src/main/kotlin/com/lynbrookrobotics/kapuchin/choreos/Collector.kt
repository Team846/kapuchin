package com.lynbrookrobotics.kapuchin.choreos

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.subsystems.collector.*
import info.kunalsheth.units.generated.*

//suspend fun Subsystems.collectorTeleop() = startChoreo("Collector teleop") {
//
//    val deployCargo by operator.deployCargo.readEagerly().withoutStamps
//    val softDeployCargo by operator.softDeployCargo.readEagerly().withoutStamps
//    val deployPanel by operator.deployPanel.readEagerly().withoutStamps
//
//    val collectCargo by driver.collectCargo.readEagerly().withoutStamps
//    val lilDicky by operator.lilDicky.readEagerly().withoutStamps
//
//    val operatorLineTracking by operator.lineTracking.readEagerly().withoutStamps
//    val centerSlider by operator.centerSlider.readEagerly().withoutStamps
//    val zeroSlider by operator.reZero.readEagerly().withoutStamps
//    val centerCargoLeft by operator.centerCargoLeft.readEagerly().withoutStamps
//    val centerCargoRight by operator.centerCargoRight.readEagerly().withoutStamps
//
//    val pivotDown by operator.pivotDown.readEagerly().withoutStamps
//    val sliderPrecision by operator.sliderPrecision.readEagerly().withoutStamps
//
//    choreography {
//        runWhenever(
//                { deployCargo || softDeployCargo } to choreography { deployCargo(softDeployCargo) },
//                { deployPanel } to choreography { deployPanel() },
//                { collectCargo } to choreography { collectCargo() },
//                { operatorLineTracking } to choreography { collectorSlider?.trackLine(lineScanner, electrical) },
//                { lilDicky } to choreography { lilDicky() },
//
//                { centerSlider } to choreography { centerSlider(0.Inch) },
//                { zeroSlider } to choreography { collectorSlider?.reZero() },
//                { centerCargoLeft } to choreography { centerCargo(true) },
//                { centerCargoRight } to choreography { centerCargo(false) },
//
//                { pivotDown } to choreography { pivotDown() },
//                { !sliderPrecision.isZero } to choreography { collectorSlider?.manualOverride(operator) }
//        )
//    }
//}

fun Subsystems.deployCargo(soft: Boolean) = choreo("Deploy cargo") {
    onStart = sequential {
        +collectorRollers?.set(
                electrical,
                if (soft) collectorRollers.cargoReleaseSpeed / 2 else collectorRollers.cargoReleaseSpeed
        )
    }
}

fun Subsystems.pivotDown() = choreo("Pivot down") {
    onStart = sequential {
        +collectorPivot?.set(CollectorPivotState.Down)
        +freeze()
    }
}

fun Subsystems.deployPanel() = choreo("Deploy panel") {
    onStart = sequential {
        +hookSlider?.set(HookSliderState.Out)
        +delay(0.2.Second)
        +hook?.set(HookPosition.Down)
        globalLaunch {
            withTimeout(1.5.Second) {
                +rumble.set(TwoSided(100.Percent, 0.Percent))
            }
        }
        +freeze()
    }

    onEnd = sequential {
        +hookSlider?.set(HookSliderState.In)
        +delay(0.5.Second)
        +hook?.set(HookPosition.Up)
    }
}

fun Subsystems.collectCargo() = choreo("Collect cargo") {
    onStart = sequential {
        parallel {
            +lift?.set(lift.cargoCollect, 0.Inch)
            +collectorPivot?.set(CollectorPivotState.Down)
            +collectorRollers?.set(electrical, collectorRollers.cargoCollectSpeed)
        }
        +freeze()
    }

    onEnd = withTimeout(1.Second) {
        +collectorRollers?.set(electrical, 8.Volt)
    }
}

fun Subsystems.lilDicky() = choreo("Lil dicky") {
    onStart = sequential {
        +hook?.set(HookPosition.Down)
        +lift?.set(lift.panelCollect, 0.Inch)
        +freeze()
    }

    onEnd = sequential {
        globalLaunch {
            +lift?.set(lift.panelCollectStroke, 0.Inch)
        }
        +delay(0.2.Second)
        +hookSlider?.set(HookSliderState.Out)
        +delay(0.2.Second)
        +hook?.set(HookPosition.Up)
        +delay(0.2.Second)
        +hookSlider?.set(HookSliderState.In)
        globalLaunch {
            withTimeout(1.5.Second) {
                +rumble.set(TwoSided(100.Percent, 0.Percent))
            }
        }
    }
}

fun Subsystems.centerSlider(tolerance: Length = 1.Inch) = choreo("Center slider") {
    onStart = sequential {
        +collectorSlider?.set(
                0.Inch,
                electrical,
                tolerance
        )
    }
}

//fun Subsystems.reZero() = choreo("Re-zero") {
//
//    collectorSlider.hardware.isZeroed = false
//
//    onStart = sequential {
//        while (!collectorSlider.hardware.isZeroed) {
//            +delay(0.2.Second)
//        }
//    }
//}

fun Subsystems.centerCargo(flip: Boolean) = choreo("Center cargo") {
    onStart = sequential {
        +collectorRollers?.set(
                electrical,
                top = if (flip) -5.Volt else 11.5.Volt,
                bottom = if (flip) 11.5.Volt else -5.Volt
        )
    }
}
