package com.lynbrookrobotics.kapuchin.routines

suspend fun collectCargo() = startChoreo("Collect Cargo") {
    // elevator to coll pos
// IH_pivot down
// IC_pivotSol down
// (meanwhile turn on IH_rollerL, IH_rollerR, IC_rollerB, IC_rollerT)
    TODO()
}

suspend fun collectWallPanel() = startChoreo("Collect Wall Panel") {
    // IC_hookSol down
// *vision tracking slider moves*
// IC_hookSliderSol out
// *probably need to wait for driver input*
// IC_hookSol up
// *pause* 
// IC_hookSliderSol in
    TODO()
}

suspend fun collectGroundPanel() = startChoreo("Collect Ground Panel") {
    //elevator handoff pos
//IH_pivot *down* (not coll) pos
//_wait for driver input_
//IH_velcroSol out (and stay out)
//IH_hookSol down
//IH_pivot *handoffpos*
    TODO()
}

suspend fun deployCargo() = startChoreo("Deploy Cargo") {
    //elevator to scoring pos (seperately input by driver)
//_auto find target_
//IC_rollerB and IC_rollerT out
    TODO()
}

suspend fun deployPanel() = startChoreo("Deploy Panel") {
    //_slider / drivetrain auto align_
//IC_hookSliderSol out
//_driver input, prob letting go of a button they hold to start this action_
//IC_hookSol down
//IC_hookSliderSol in
    TODO()
}

suspend fun pushPanel() = startChoreo("Push Panel") {
//idk what 6 is,
    TODO()
}

suspend fun unleashTheCobra() = startChoreo("Unleash the Cobra") {
//just goo 2 motors until some sort of sensor
    TODO()
}