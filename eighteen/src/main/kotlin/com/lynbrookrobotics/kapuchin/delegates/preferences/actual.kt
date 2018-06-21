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

actual fun nameLayer(thisRef: Named, layer: String) =
        "${thisRef.name}/$layer"

actual fun preference(fallback: Boolean) = Preference(fallback, impl::getBoolean)
actual fun preference(fallback: Double) = Preference(fallback, impl::getDouble)
actual fun preference(fallback: Float) = Preference(fallback, impl::getFloat)
actual fun preference(fallback: Int) = Preference(fallback, impl::getInt)
actual fun preference(fallback: Long) = Preference(fallback, impl::getLong)

private fun <Q : Quan<Q>> setupUom(uomFunc: KProperty0<Q>): Pair<Double, (Double) -> Q> {
    val uom = uomFunc()
    val uomValue = (uomFunc.extensionReceiverParameter as Number).toDouble()
    val conversionFactor = uom.siValue / uomValue

    return uomValue to { it -> uom.new(it * conversionFactor) }
}

actual fun <Q : Quan<Q>> preference(fallback: KProperty0<Q>): UomPreference<Q> {
    val (uomValue, uomConversion) = setupUom(fallback)
    return UomPreference(uomConversion, fallback.name, uomValue, impl::getDouble)
}