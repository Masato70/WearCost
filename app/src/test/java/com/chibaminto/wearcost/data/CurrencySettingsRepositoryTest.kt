package com.chibaminto.wearcost.data

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class CurrencySettingsRepositoryTest {
    @Test
    fun inferCurrencyCode_usesSupportedRegionCurrencies() {
        assertEquals("USD", inferCurrencyCode(Locale.US))
        assertEquals("JPY", inferCurrencyCode(Locale.JAPAN))
        assertEquals("KRW", inferCurrencyCode(Locale.KOREA))
        assertEquals("CNY", inferCurrencyCode(Locale.CHINA))
        assertEquals("GBP", inferCurrencyCode(Locale.UK))
        assertEquals("EUR", inferCurrencyCode(Locale.FRANCE))
    }
}
