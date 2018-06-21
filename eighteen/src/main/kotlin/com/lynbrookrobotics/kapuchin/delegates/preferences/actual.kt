package com.lynbrookrobotics.kapuchin.delegates.preferences

import com.lynbrookrobotics.kapuchin.Quan
import com.lynbrookrobotics.kapuchin.subsystems.Named
import edu.wpi.first.wpilibj.Preferences
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0
import kotlin.reflect.full.extensionReceiverParameter

private val impl = Preferences.getInstance()

actual fun namePreference(thisRef: Named, prop: KProperty<*>) =
        "${thisRef.name}/${prop.name}"

actual fun Named.preference(fallback: Boolean) = Preference(this, fallback, impl::getBoolean)
actual fun Named.preference(fallback: Double) = Preference(this, fallback, impl::getDouble)
actual fun Named.preference(fallback: Float) = Preference(this, fallback, impl::getFloat)
actual fun Named.preference(fallback: Int) = Preference(this, fallback, impl::getInt)
actual fun Named.preference(fallback: Long) = Preference(this, fallback, impl::getLong)
actual fun <Q : Quan<Q>> Named.preference(fallback: KProperty0<Q>): UomPreference<Q> {
    val uom = fallback()
    val uomValue = (fallback.extensionReceiverParameter as Number).toDouble()
    val conversionFactor = uom.siValue / uomValue

    return UomPreference(
            { it -> uom.new(it * conversionFactor) },
            fallback.name, this, uomValue, impl::getDouble
    )
}