package com.lynbrookrobotics.kapuchin.logging

import com.lynbrookrobotics.kapuchin.hardware.*
import info.kunalsheth.units.generated.*

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
expect class Grapher<Q : Quan<Q>> internal constructor(parent: Named, of: String, withUnits: UomConverter<Q>) : Named, (Time, Q) -> Unit {
    override fun invoke(x: Time, y: Q)

    fun flush()
    fun close()
}

/**
 * Public graph initializer
 *
 * @receiver owner of this graph
 * @param Q type of data being graphed
 * @param of graph title
 * @param withUnits y-axis units
 * @return new `Grapher` instance
 */
fun <Q : Quan<Q>> Named.graph(of: String, withUnits: UomConverter<Q>, parent: Named = this) = Grapher(parent, of, withUnits)