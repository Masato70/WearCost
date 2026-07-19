package com.chibaminto.wearcost.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Currency
import java.util.Locale

class CurrencySettingsRepository(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(
        "wearcost_settings",
        Context.MODE_PRIVATE
    )
    private val _currencyCode = MutableStateFlow(loadCurrencyCode())
    val currencyCode: StateFlow<String> = _currencyCode

    fun setCurrencyCode(code: String) {
        if (code !in SupportedCurrencyCodes) return
        preferences.edit().putString(KEY_CURRENCY_CODE, code).apply()
        _currencyCode.value = code
    }

    private fun loadCurrencyCode(): String {
        val savedCurrencyCode = preferences.getString(KEY_CURRENCY_CODE, null)
        if (savedCurrencyCode != null && savedCurrencyCode in SupportedCurrencyCodes) return savedCurrencyCode

        val inferredCurrencyCode = inferCurrencyCode(Locale.getDefault())
        preferences.edit().putString(KEY_CURRENCY_CODE, inferredCurrencyCode).apply()
        return inferredCurrencyCode
    }

    companion object {
        private const val KEY_CURRENCY_CODE = "currency_code"
    }
}

val SupportedCurrencyCodes = listOf("JPY", "USD", "EUR", "GBP", "KRW", "CNY")

fun inferCurrencyCode(locale: Locale): String {
    val country = locale.country.uppercase(Locale.US)
    if (country == "JP") return "JPY"
    if (country == "KR") return "KRW"
    if (country == "CN") return "CNY"
    if (country == "US") return "USD"
    if (country == "GB") return "GBP"

    return runCatching { Currency.getInstance(locale).currencyCode }
        .getOrNull()
        ?.takeIf { it in SupportedCurrencyCodes }
        ?: "USD"
}
