package com.lynbrookrobotics.kapuchin

import com.lynbrookrobotics.kapuchin.subsystems.Component
import info.kunalsheth.units.generated.Quantity

typealias Comp = Component<*, *, *>
typealias Quan<Q> = Quantity<Q, *, *>

infix fun <A, B> ((A) -> B).dependsOn(f: () -> A?): B? =
        try {
            this(f()!!)
        } catch (t: Throwable) {
            null
        }

infix fun <A, B, C> ((A, B) -> C).dependsOn(f: Pair<() -> A?, B?>): C? =
        try {
            this(f.first()!!, f.second!!)
        } catch (t: Throwable) {
            null
        }

infix fun <A, B, C, D> ((A, B, C) -> D).dependsOn(f: Pair<Pair<() -> A?, B?>, C?>): D? =
        try {
            this(f.first.first()!!, f.first.second!!, f.second!!)
        } catch (t: Throwable) {
            null
        }