package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.subsystems.Component
import com.lynbrookrobotics.kapuchin.subsystems.SubsystemHardware
import info.kunalsheth.units.generated.Time
import kotlinx.coroutines.experimental.suspendCancellableCoroutine

suspend fun <C, H, Output> C.autoroutine(
        newController: C.(Time) -> Output,
        isFinished: C.(Time) -> Boolean
)
        where C : Component<C, H, Output>,
              H : SubsystemHardware<H, C> =

        suspendCancellableCoroutine<Unit> { cont ->
            var cancelTicker = {}
            try {
                cancelTicker = ticker.runOnTick { if (isFinished(it)) cont.resume(Unit) }
                controller = newController
            } finally {
                cancelTicker()
                controller = null
            }
        }