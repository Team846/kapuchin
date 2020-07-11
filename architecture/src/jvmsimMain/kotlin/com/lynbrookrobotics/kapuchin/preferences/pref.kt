package com.lynbrookrobotics.kapuchin.preferences

import com.lynbrookrobotics.kapuchin.logging.*
import info.kunalsheth.units.generated.*

private fun <X> f(x: X): (Any?, Any?) -> X = { _, _ -> x }

actual fun Named.pref(fallback: Boolean) =
    Preference(this, fallback, f(Unit), f(fallback), { true }, ::registerCallback)

actual fun Named.pref(fallback: Double) = Preference(this, fallback, f(Unit), f(fallback), { true }, ::registerCallback)
actual fun Named.pref(fallback: Float) = Preference(this, fallback, f(Unit), f(fallback), { true }, ::registerCallback)
actual fun Named.pref(fallback: Int) = Preference(this, fallback, f(Unit), f(fallback), { true }, ::registerCallback)
actual fun Named.pref(fallback: Long) = Preference(this, fallback, f(Unit), f(fallback), { true }, ::registerCallback)
actual fun Named.pref(fallback: String) = Preference(this, fallback, f(Unit), f(fallback), { true }, ::registerCallback)
actual fun <Q : Quan<Q>> Named.pref(fallback: Number, withUnits: UomConverter<Q>) =
    Preference(
        this,
        withUnits(fallback.toDouble()),
        f(Unit),
        f(withUnits(fallback.toDouble())),
        { true },
        ::registerCallback
    )

/**
 * No-op registerCallback function to pass through as a parameter for Preference
 *
 * @author Andy
 */
private fun registerCallback(a: String, b: () -> Unit) {

}