package com.lynbrookrobotics.kapuchin.logging

import com.lynbrookrobotics.kapuchin.control.Quan
import info.kunalsheth.units.generated.Time
import info.kunalsheth.units.generated.UomConverter

expect class Grapher<Q : Quan<Q>> private constructor(parent: Named, of: String, withUnits: UomConverter<Q>) : Named, (Time, Q) -> Unit {
    override fun invoke(stamp: Time, value: Q)

    companion object {
        fun <Q : Quan<Q>> Named.graph(of: String, withUnits: UomConverter<Q>): Grapher<Q>
    }
}