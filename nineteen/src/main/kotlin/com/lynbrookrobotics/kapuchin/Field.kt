package com.lynbrookrobotics.kapuchin

import com.lynbrookrobotics.kapuchin.control.data.*
import info.kunalsheth.units.generated.*

//Based on the CAD diagram of the field.

//HAB
val leftHAB = UomVector(-21.33333.Inch, 82.875.Inch)
val centerHAB = UomVector(0.Inch, 82.85.Inch)
val rightHAB = UomVector(21.33333.Inch, 82.85.Inch)

//Cargo
val cargoYSeparation = 21.75.Inch
val closeCargoY = 259.625.Inch
val middleCargoY = closeCargoY + cargoYSeparation
val farCargoY = closeCargoY + cargoYSeparation * 2.0
val cargoX = 45.875.Inch 

val leftCloseCargo = UomVector(cargoX, closeCargoY)
val leftMiddleCargo = UomVector(cargoX, middleCargoY)
val leftFarCargo = UomVector(cargoX, farCargoY)

val rightCloseCargo = UomVector(-cargoX, closeCargoY)
val rightMiddleCargo = UomVector(-cargoX, middleCargoY)
val rightFarCargo = UomVector(-cargoX, farCargoY)

val leftFrontCargo = UomVector(-10.875.Inch, 201.375.Inch)
val rightFrontCargo = UomVector(10.875.Inch, 201.375.Inch)

//Rocket
val closeRocketX = 134.222.Inch
val closeRocketY = 197.58.Inch

val middleRocketX = 115.558.Inch
val middleRocketY = 228.Inch

val farRocketX = 134.222.Inch
val farRocketY = 258.413.Inch

val leftCloseRocket = UomVector(-closeRocketX, closeRocketY)
val leftMiddleRocket = UomVector(-middleRocketX, middleRocketY)
val leftFarRocket = UomVector(-farRocketX, farRocketY)

val rightCloseRocket = UomVector(closeRocketX, closeRocketY)
val rightMiddleRocket = UomVector(middleRocketX, middleRocketY)
val rightFarRocket = UomVector(farRocketX, farRocketY)

//Loading station
val leftLoadingStation = UomVector(-135.285.Inch, 16.687.Inch)
val rightLoadingStation = UomVector(135.285.Inch, 16.687.Inch)





