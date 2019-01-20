package com.lynbrookrobotics.kapuchin.preferences

import com.lynbrookrobotics.kapuchin.logging.Named
import edu.wpi.first.wpilibj.Preferences
import info.kunalsheth.units.generated.Quan
import info.kunalsheth.units.generated.UomConverter

private val impl = Preferences.getInstance()

actual fun Named.pref(fallback: Boolean) = Preference(this, fallback, impl::putBoolean, impl::getBoolean)
actual fun Named.pref(fallback: Double) = Preference(this, fallback, impl::putDouble, impl::getDouble)
actual fun Named.pref(fallback: Float) = Preference(this, fallback, impl::putFloat, impl::getFloat)
actual fun Named.pref(fallback: Int) = Preference(this, fallback, impl::putInt, impl::getInt)
actual fun Named.pref(fallback: Long) = Preference(this, fallback, impl::putLong, impl::getLong)
actual fun Named.pref(fallback: String) = Preference(this, fallback, impl::putString, impl::getString)
actual fun <Q : Quan<Q>> Named.pref(fallback: Number, withUnits: UomConverter<Q>) = Preference(
        this, withUnits(fallback.toDouble()),
        { name, value -> impl.putDouble(name, withUnits(value)) },
        { name, value -> withUnits(impl.getDouble(name, withUnits(value))) },
        " (${withUnits.unitName})"
)