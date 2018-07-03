package com.lynbrookrobotics.kapuchin.preferences

import com.lynbrookrobotics.kapuchin.control.Quan
import com.lynbrookrobotics.kapuchin.logging.Named
import edu.wpi.first.wpilibj.Preferences
import info.kunalsheth.units.generated.UomConverter

private val impl = Preferences.getInstance()

actual fun Named.pref(fallback: Boolean) = Preference(this, fallback, impl::putBoolean, impl::getBoolean)
actual fun Named.pref(fallback: Double) = Preference(this, fallback, impl::putDouble, impl::getDouble)
actual fun Named.pref(fallback: Float) = Preference(this, fallback, impl::putFloat, impl::getFloat)
actual fun Named.pref(fallback: Int) = Preference(this, fallback, impl::putInt, impl::getInt)
actual fun Named.pref(fallback: Long) = Preference(this, fallback, impl::putLong, impl::getLong)
actual fun <Q : Quan<Q>> Named.pref(fallback: Number, withUnits: UomConverter<Q>) = Preference(
        this, withUnits(fallback),
        { name, _ -> impl.putDouble(name, fallback.toDouble()) },
        { name, _ -> withUnits(impl.getDouble(name, fallback.toDouble())) },
        " (${withUnits.unitName})"
)