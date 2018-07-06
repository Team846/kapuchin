package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.logging.Named
import com.lynbrookrobotics.kapuchin.subsystems.Component
import com.lynbrookrobotics.kapuchin.subsystems.SubsystemHardware
import info.kunalsheth.units.generated.Time
import kotlinx.coroutines.experimental.suspendCancellableCoroutine

class Routine<C, H, Output> private constructor(
        val parent: C, name: String,
        val controller: C.(Time) -> Output,
        val isFinished: C.(Time) -> Boolean,
        val stop: () -> Unit
) : Named("$name Routine", parent)
        where C : Component<C, H, Output>,
              H : SubsystemHardware<H, C> {

    companion object {
        suspend fun <C, H, Output> C.runRoutine(
                name: String,
                newController: C.(Time) -> Output,
                isFinished: C.(Time) -> Boolean
        )
                where C : Component<C, H, Output>,
                      H : SubsystemHardware<H, C> =

                suspendCancellableCoroutine<Unit> { cont ->
                    routine = Routine(this, name, newController, isFinished) { cont.resume(Unit) }
                }
    }
}