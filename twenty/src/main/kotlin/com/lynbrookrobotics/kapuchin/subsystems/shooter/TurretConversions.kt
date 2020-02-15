package com.lynbrookrobotics.kapuchin.subsystems.shooter

import com.lynbrookrobotics.kapuchin.control.conversion.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.preferences.*
import info.kunalsheth.units.generated.*

class TurretConversions(hardware: TurretHardware) : Named by Named("Conversions", hardware) {
   val gearbox by pref {
      val input = 1
      val output = 10
      ({ GearTrain(input, output) })
   }
   val driveWheelRadius by pref(1, Inch)
   val lazySusanRadius by pref(13, Inch)

   fun turretToMotor(turret: Angle): Angle = gearbox.outputToInput(
           turret * driveWheelRadius / lazySusanRadius
   )
   fun motorToTurret(motor: Angle): Angle = gearbox.inputToOutput(motor) * lazySusanRadius / driveWheelRadius
}