package com.lynbrookrobotics.kapuchin

import com.lynbrookrobotics.kapuchin.control.data.*
import info.kunalsheth.units.generated.*

//Based on the CAD diagram of the field.

//HAB
val leftHAB = Position(-21.33333.Inch, 82.875.Inch, 0.Degree)
val centerHAB = Position(0.Inch, 82.875.Inch, 0.Degree)
val rightHAB = Position(21.33333.Inch, 82.875.Inch, 0.Degree)

//Cargo
val cargoYSeparation = 21.75.Inch
val closeCargoY = 259.625.Inch
val middleCargoY = closeCargoY + cargoYSeparation
val farCargoY = closeCargoY + cargoYSeparation * 2.0
val cargoX = 45.875.Inch 

val leftCloseCargo = Position(cargoX, closeCargoY, 90.Degree)
val leftMiddleCargo = Position(cargoX, middleCargoY, 90.Degree)
val leftFarCargo = Position(cargoX, farCargoY, 90.Degree)

val rightCloseCargo = Position(-cargoX, closeCargoY, -90.Degree)
val rightMiddleCargo = Position(-cargoX, middleCargoY, -90.Degree)
val rightFarCargo = Position(-cargoX, farCargoY, -90.Degree)

val leftFrontCargo = Position(-10.875.Inch, 201.375.Inch, 0.Degree)
val rightFrontCargo = Position(10.875.Inch, 201.375.Inch, 0.Degree)

//Rocket
val closeRocketX = 134.222.Inch
val closeRocketY = 197.587.Inch

val middleRocketX = 115.558.Inch
val middleRocketY = 228.Inch

val farRocketX = 134.222.Inch
val farRocketY = 258.413.Inch

val leftCloseRocket = Position(-closeRocketX, closeRocketY, -30.Degree)
val leftMiddleRocket = Position(-middleRocketX, middleRocketY, -90.Degree)
val leftFarRocket = Position(-farRocketX, farRocketY, -150.Degree)

val rightCloseRocket = Position(closeRocketX, closeRocketY, 30.Degree)
val rightMiddleRocket = Position(middleRocketX, middleRocketY, 90.Degree)
val rightFarRocket = Position(farRocketX, farRocketY, 150.Degree)

//Loading station
val leftLoadingStation = Position(-135.285.Inch, 16.687.Inch, 180.Degree)
val rightLoadingStation = Position(135.285.Inch, 16.687.Inch, 180.Degree)





