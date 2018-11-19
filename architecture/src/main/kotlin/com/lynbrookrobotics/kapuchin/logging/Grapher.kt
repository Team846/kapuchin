package com.lynbrookrobotics.kapuchin.logging

import com.lynbrookrobotics.kapuchin.hardware.Sensor
import info.kunalsheth.units.generated.Quan
import info.kunalsheth.units.generated.Time
import info.kunalsheth.units.generated.UomConverter

/**
 * Graphs data over time
 *
 * Implementations should be accessible from real-time control code
 *
 * @author Kunal
 * @see Sensor
 *
 * @param Q type of data being graphed
 */
expect class Grapher<Q : Quan<Q>> private constructor(parent: Named, of: String, withUnits: UomConverter<Q>) : Named, (Time, Q) -> Unit {
    override fun invoke(x: Time, y: Q)

    fun flush()
    fun close()

    companion object {
        /**
         * Public graph initializer
         *
         * @receiver owner of this graph
         * @param Q type of data being graphed
         * @param of graph title
         * @param withUnits y-axis units
         * @return new `Grapher` instance
         */
        fun <Q : Quan<Q>> Named.graph(of: String, withUnits: UomConverter<Q>): Grapher<Q>
    }
}