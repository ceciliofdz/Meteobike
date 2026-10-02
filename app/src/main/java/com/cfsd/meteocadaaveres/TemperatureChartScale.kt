package com.cfsd.meteocadaaveres

/** Vertical positions from top (0) to bottom (1), centered for constant data. */
internal fun temperatureChartFractions(temperatures: List<Float>): List<Float> {
    if (temperatures.isEmpty()) return emptyList()
    require(temperatures.all { it.isFinite() })

    val min = temperatures.min().toDouble()
    val max = temperatures.max().toDouble()
    val range = max - min
    if (range == 0.0) return temperatures.map { 0.5f }

    return temperatures.map { ((max - it.toDouble()) / range).toFloat() }
}
