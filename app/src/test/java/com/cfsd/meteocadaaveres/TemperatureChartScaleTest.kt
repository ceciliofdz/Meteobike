package com.cfsd.meteocadaaveres

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TemperatureChartScaleTest {
    @Test fun noTemperaturesProducesNoPoints() {
        assertTrue(temperatureChartFractions(emptyList()).isEmpty())
    }

    @Test fun singleTemperatureIsVisibleInTheMiddle() {
        assertEquals(listOf(0.5f), temperatureChartFractions(listOf(18f)))
    }

    @Test fun equalTemperaturesFormAHorizontalLine() {
        for (temperature in listOf(-5f, 0f, 18f)) {
            assertEquals(
                listOf(0.5f, 0.5f, 0.5f),
                temperatureChartFractions(List(3) { temperature })
            )
        }
    }

    @Test fun varyingTemperaturesKeepTheirOrderAndRelativeHeight() {
        assertEquals(
            listOf(1f, 0.5f, 0f, 0.75f),
            temperatureChartFractions(listOf(-10f, 0f, 10f, -5f))
        )
    }

    @Test fun extremeFiniteValuesDoNotOverflowTheScale() {
        assertEquals(
            listOf(1f, 0.5f, 0f),
            temperatureChartFractions(listOf(-Float.MAX_VALUE, 0f, Float.MAX_VALUE))
        )
    }
}
