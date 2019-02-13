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
    TODO()
}

suspend fun deployCargo() = startChoreo("Deploy Cargo") {
    TODO()
}

suspend fun deployPanel() = startChoreo("Deploy Panel") {
    TODO()
}

suspend fun pushPanel() = startChoreo("Push Panel") {
    TODO()
}

suspend fun unleashTheCobra() = startChoreo("Unleash the Cobra") {
    TODO()
}