package com.lynbrookrobotics.kapuchin

import com.lynbrookrobotics.kapuchin.control.data.*
import info.kunalsheth.units.generated.*

//Based on the CAD diagram of the field.

//HAB
val leftHAB = UomVector(-259.125.Inch, 42.66666667.Inch)
val centerHAB = UomVector(-259.125.Inch, 0.Inch)
val rightHAB = UomVector(-259.125.Inch, -42.66666667.Inch)

//Cargo
val cargoXSeparation = 21.75.Inch
val farCargoX = -64.375.Inch
val middleCargoX = farCargoX + cargoXSeparation
val closeCargoX = farCargoX + cargoXSeparation * 2.0
val cargoY = 45.875.Inch

val leftCloseCargo = UomVector(farCargoX, cargoY)
val leftMiddleCargo = UomVector(middleCargoX, cargoY)
val leftFarCargo = UomVector(closeCargoX, cargoY)

val rightCloseCargo = UomVector(farCargoX, -cargoY)
val rightMiddleCargo = UomVector(middleCargoX, -cargoY)
val rightFarCargo = UomVector(closeCargoX, -cargoY)

val leftFrontCargo = UomVector(-122.625.Inch, 10.875.Inch)
val rightFrontCargo = UomVector(-122.625.Inch, 10.875.Inch)

//Rocket
val closeRocketX = -126.625.Inch
val closeRocketY = 134.222.Inch

val middleRocketX = -96.Inch
val middleRocketY = 115.558.Inch

val farRocketX = -65.587.Inch
val farRocketY = 134.222.Inch

val leftCloseRocket = UomVector(closeRocketX, closeRocketY)
val leftMiddleRocket = UomVector(middleRocketX, middleRocketY)
val leftFarRocket = UomVector(farRocketX, farRocketY)

val rightCloseRocket = UomVector(closeRocketX, -closeRocketY)
val rightMiddleRocket = UomVector(middleRocketX, -middleRocketY)
val rightFarRocket = UomVector(farRocketX, -farRocketY)

//Loading station
val leftLoadingStation = UomVector(-307.313.Inch, 135.285.Inch)
val rightLoadingStation = UomVector(-307.313.Inch, -135.285.Inch)





