package com.lynbrookrobotics.kapuchin.choreos

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.subsystems.shooter.*
import info.kunalsheth.units.generated.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

suspend fun Subsystems.shooterTeleop() = startChoreo("Shooter Teleop") {


    choreography {

        val shoot by operator.shoot.readEagerly().withoutStamps
        val turret by operator.turret.readEagerly().withoutStamps

        runWhenever(

                { shoot } to choreography { shoot() },
                { turret } to choreography { turret() }

        )

    }


}



suspend fun Subsystems.shoot() = supervisorScope{
    val feederRollerVolt by pref (6, Volt)
    val shooterVolt by pref (6, Volt)

    var roller: Job? = null
    var spin: Job? = null
    try{
        roller = launch { feederRoller?.spin(electrical, feederRollerVolt) }
        spin = launch { shooter?.spin(electrical, shooterVolt, shooterVolt ) }
        freeze()
    }

    finally{
        roller?.cancel()
        spin?.cancel()

    }


}

suspend fun Subsystems.turret() = supervisorScope{
    val turretVolt by pref (6, Volt)
    var spin: Job? = null
    try{
        spin = launch { turret?.spin(electrical, turretVolt) }
        freeze()
    }

    finally{
        spin?.cancel()

    }


}

