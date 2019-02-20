package com.lynbrookrobotics.kapuchin.choreos

import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.collector.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.collector.HookPosition.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.collector.HookSliderPosition.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.handoff.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.handoff.pivot.*
import com.lynbrookrobotics.kapuchin.subsystems.lift.*
import kotlinx.coroutines.launch

suspend fun collectCargo(
        lift: LiftComponent,
        handoffPivot: HandoffPivotComponent,
        collectorPivot: CollectorPivotComponent,
        handoffRollers: HandoffRollersComponent,
        collectorRollers: CollectorRollersComponent
) = startChoreo("Collect Cargo") {
    // elevator to coll pos
// IH_pivot down
// IC_pivotSol down
// (meanwhile turn on IH_rollerL, IH_rollerR, IC_rollerB, IC_rollerT)
    choreography {
        launch { handoffRollers.spin(handoffRollers.cargoCollectSpeed) }
        launch { collectorRollers.spin(collectorRollers.cargoCollectSpeed) }
        lift.to(lift.collectHeight)
        handoffPivot.to(handoffPivot.collectPosition)
        collectorPivot.to(CollectorPivotPosition.Down)
    }
}

suspend fun collectWallPanel(
        hook: HookComponent,
        hookSlider: HookSliderComponent
) = startChoreo("Collect Wall Panel") {
    // IC_hookSol down
// *vision tracking slider moves*
// IC_hookSliderSol out
// *probably need to wait for driver input*
// IC_hookSol up
// *pause*
// IC_hookSliderSol in
    choreography {
        hook.to(Down)
        //visiontracking
        hookSlider.to(Out)
        //driver input
        hook.to(Up)
        hookSlider.to(In)
    }
}

suspend fun collectGroundPanel(
        lift: LiftComponent,
        hook: HookComponent,
        handoffPivot: HandoffPivotComponent
) = startChoreo("Collect Ground Panel") {
    //elevator handoff pos
//IH_pivot *down* (not coll) pos
//_wait for driver input_
//IH_velcroSol out (and stay out)
//IH_hookSol down
//IH_pivot *handoffpos*
    choreography {
        lift.to(lift.collectHeight)
        //handoffPivot.to(handoffPivot.plateHandoffPosition)
        //driver input
        hook.to(Down)
        handoffPivot.to(handoffPivot.plateHandoffPosition)
    }
}

suspend fun deployCargo(
        lift: LiftComponent,
        collectorRollers: CollectorRollersComponent
) = startChoreo("Deploy Cargo") {
    //elevator to scoring pos (seperately input by driver)
//_auto find target_
//IC_rollerB and IC_rollerT out
    choreography {
        //lift
        //find target
        collectorRollers.spin(collectorRollers.cargoReleaseSpeed)
    }
}

suspend fun deployPanel(
        hookSlider: HookSliderComponent,
        hook: HookComponent
) = startChoreo("Deploy Panel") {
    //_slider / drivetrain auto align_
//IC_hookSliderSol out
//_driver input, prob letting go of a button they hold to start this action_
//IC_hookSol down
//IC_hookSliderSol in
    choreography {
        //autoalign
        hookSlider.to(Out)
        //let go of button
        hook.to(Down)
        hookSlider.to(In)
    }
}

suspend fun pushPanel(
        hookSlider: HookSliderComponent,
        hook: HookComponent
) = startChoreo("Push Panel") {
    //deployPanel without autoalign
    choreography {
        hookSlider.to(Out)
        //let go of button
        hook.to(Down)
        hookSlider.to(In)
    }
}

suspend fun unleashTheCobra(
        climber: ClimberComponent
) = startChoreo("Unleash the Cobra") {
    //just goo 2 motors until some sort of sensor
    choreography {
        climber.to(climber.maxOutput)
    }
}
