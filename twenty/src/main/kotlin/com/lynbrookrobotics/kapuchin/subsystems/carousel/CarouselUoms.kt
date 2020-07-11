package com.lynbrookrobotics.kapuchin.subsystems.carousel

import info.kunalsheth.units.generated.*
import kotlin.math.PI

inline val Number.CarouselSlot: Angle get() = `∠`(toDouble() * (2 * PI) / 5)
inline val `∠`.CarouselSlot get() = siValue * 5 / (2 * PI)

object CarouselSlot : UomConverter<`∠`>,
    Quan<`∠`> by box(1.CarouselSlot) {
    override val unitName = "CarouselSlot"
    override fun invoke(x: Double) = x.CarouselSlot
    override fun invoke(x: `∠`) = x.CarouselSlot
}