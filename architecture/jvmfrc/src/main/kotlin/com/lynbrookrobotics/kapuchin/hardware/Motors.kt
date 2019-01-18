package com.lynbrookrobotics.kapuchin.hardware

import com.lynbrookrobotics.kapuchin.control.data.Motor
import info.kunalsheth.units.generated.Ampere
import info.kunalsheth.units.generated.NewtonMetre
import info.kunalsheth.units.generated.Rpm
import info.kunalsheth.units.generated.Volt

// https://motors.vex.com
enum class CommonMotors(val spec: Motor) {
    cim(Motor(12.Volt, 5330.Rpm, 131.Ampere, 2.41.NewtonMetre)),
    miniCim(Motor(12.Volt, 5840.Rpm, 89.Ampere, 1.41.NewtonMetre)),
    bag(Motor(12.Volt, 13180.Rpm, 53.Ampere, 0.43.NewtonMetre)),
    sevenSevenFivePro(Motor(12.Volt, 18730.Rpm, 134.Ampere, 0.71.NewtonMetre)),
    am9015(Motor(12.Volt, 14270.Rpm, 71.Ampere, 0.36.NewtonMetre)),
    amNeveRest(Motor(12.Volt, 5480.Rpm, 10.Ampere, 0.17.NewtonMetre)),
    amRS775125(Motor(12.Volt, 5800.Rpm, 18.Ampere, 0.28.NewtonMetre)),
    bbRS775(Motor(12.Volt, 13050.Rpm, 97.Ampere, 0.72.NewtonMetre)),
    bbRS550(Motor(12.Volt, 19000.Rpm, 84.Ampere, 0.38.NewtonMetre))
}