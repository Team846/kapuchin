package com.lynbrookrobotics.kapuchin.preferences

import com.lynbrookrobotics.kapuchin.logging.Named
import edu.wpi.first.wpilibj.Preferences
import info.kunalsheth.units.generated.Quan
import info.kunalsheth.units.generated.UomConverter

private val impl = Preferences.getInstance()
private fun <T> Preference<T>.f() = also { Preferences2.getInstance().registerCallback(this) }


actual fun Named.pref(fallback: Boolean) = Preference(this, fallback, impl::putBoolean, impl::getBoolean).f()
actual fun Named.pref(fallback: Double) = Preference(this, fallback, impl::putDouble, impl::getDouble).f()
actual fun Named.pref(fallback: Float) = Preference(this, fallback, impl::putFloat, impl::getFloat).f()
actual fun Named.pref(fallback: Int) = Preference(this, fallback, impl::putInt, impl::getInt).f()
actual fun Named.pref(fallback: Long) = Preference(this, fallback, impl::putLong, impl::getLong).f()
actual fun Named.pref(fallback: String) = Preference(this, fallback, impl::putString, impl::getString).f()

actual fun <Q : Quan<Q>> Named.pref(fallback: Number, withUnits: UomConverter<Q>) = Preference(
        this, withUnits(fallback.toDouble()),
        { name, value -> impl.putDouble(name, withUnits(value)) },
        { name, value -> withUnits(impl.getDouble(name, withUnits(value))) },
        " (${withUnits.unitName})"
).f()
