package com.lynbrookrobotics.kapuchin.choreos

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.routines.*
import info.kunalsheth.units.generated.*

//suspend fun Subsystems.climberTeleop() = startChoreo("Climber teleop") {
//
//    val unleashTheCobra by operator.unleashTheCobra.readEagerly().withoutStamps
//    val oShitSnekGoBack by operator.oShitSnekGoBack.readEagerly().withoutStamps
//
//    choreography {
//        runWhenever(
//                { unleashTheCobra } to choreography { unleashTheCobra() },
//                { oShitSnekGoBack } to choreography { climber?.set(-climber.maxOutput / 2) ?: freeze() }
//        )
//    }
//}

fun Subsystems.unleashTheCobra() = choreo("Unleash the cobra") {
    onStart = parallel {
        sequential {
            +delay(0.5.Second)
            globalLaunch {
                +leds?.rainbow()
            }
            +drivetrain.openLoop(30.Percent)
        }

        +leds?.rainbow()
        +climber?.set(climber.maxOutput)
        +freeze()
    }
}

fun Subsystems.oShitSnekGoBack() = choreo("O shit snek go back") {
    onStart = sequential {
        +climber?.set(-climber.maxOutput / 2)
        +freeze()
    }
}