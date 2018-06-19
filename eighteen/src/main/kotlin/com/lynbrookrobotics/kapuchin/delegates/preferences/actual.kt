package com.lynbrookrobotics.kapuchin.delegates.preferences

import com.lynbrookrobotics.kapuchin.Quan
import com.lynbrookrobotics.kapuchin.subsystems.Named
import edu.wpi.first.wpilibj.Preferences
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

val impl = Preferences.getInstance()

actual fun namePreference(thisRef: Named, prop: KProperty<*>) =
        "${thisRef.name}/${prop.name}"

actual fun preference(fallback: Boolean) = Preference(fallback, impl::getBoolean)
actual fun preference(fallback: Double) = Preference(fallback, impl::getDouble)
actual fun preference(fallback: Float) = Preference(fallback, impl::getFloat)
actual fun preference(fallback: Int) = Preference(fallback, impl::getInt)
actual fun preference(fallback: Long) = Preference(fallback, impl::getLong)

actual fun <Q : Quan<Q>> preference(fallback: Double, conversion: KProperty1<Double, Q>) =
        UomPreference(fallback, conversion, impl::getDouble)

actual fun <Error : Quan<Error>, Compensation : Quan<Compensation>> preference(
        fallbackError: Double, errorConversion: KProperty1<Double, Error>,
        fallbackComp: Double, compConversion: KProperty1<Double, Compensation>
): GainPreference<Error, Compensation> =
        GainPreference(fallbackError, errorConversion, fallbackComp, compConversion, impl::getDouble)