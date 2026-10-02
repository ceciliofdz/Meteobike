package com.cfsd.meteocadaaveres

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.junit.Assert.assertEquals
import org.junit.Test

class WeatherJsonTest {
    private val gson = Gson()

    @Test fun readsPreviouslySavedLocation() {
        val json = """{"nombre":"Getafe","codigo":"28065","provincia":"Madrid"}"""
        assertEquals(Localidad("Getafe", "28065", "Madrid"), gson.fromJson(json, Localidad::class.java))
    }

    @Test fun readsCatalogThroughBothModelTypes() {
        val json = """[{"source":"Madrid","data":{"Getafe":"28065"}}]"""
        val type = object : TypeToken<List<ProvinciaLocalidades>>() {}.type
        val provinces: List<ProvinciaLocalidades> = gson.fromJson(json, type)
        val wrappers = gson.fromJson(json, Array<LocalidadWrapper>::class.java)
        assertEquals("28065", provinces.single().data["Getafe"])
        assertEquals(provinces.single().data, wrappers.single().data)
    }

    @Test fun dailyNavigationPreservesNestedPrecipitationPeriods() {
        val day = DiaPrediccion("2026-10-02", "20", "Despejado", "22", "12", "80", "40", "N", "10", listOf("00-06" to "20"))
        assertEquals(day, gson.fromJson(gson.toJson(day), DiaPrediccion::class.java))
    }
}
