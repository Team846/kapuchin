package com.lynbrookrobotics.kapuchin.preferences

import com.lynbrookrobotics.kapuchin.control.Quan
import com.lynbrookrobotics.kapuchin.logging.Named
import info.kunalsheth.units.generated.UomConverter

private fun <X> f(x: X): (Any?, Any?) -> X = { _, _ -> x }

actual fun Named.pref(fallback: Boolean) = Preference(this, fallback, f(Unit), f(fallback))
actual fun Named.pref(fallback: Double) = Preference(this, fallback, f(Unit), f(fallback))
actual fun Named.pref(fallback: Float) = Preference(this, fallback, f(Unit), f(fallback))
actual fun Named.pref(fallback: Int) = Preference(this, fallback, f(Unit), f(fallback))
actual fun Named.pref(fallback: Long) = Preference(this, fallback, f(Unit), f(fallback))
actual fun <Q : Quan<Q>> Named.pref(fallback: Number, withUnits: UomConverter<Q>) =
        Preference(this, withUnits(fallback), f(Unit), f(withUnits(fallback)))