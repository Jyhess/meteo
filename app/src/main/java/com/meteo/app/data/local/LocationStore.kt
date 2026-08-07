package com.meteo.app.data.local

import android.content.Context
import androidx.core.content.edit
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import com.meteo.app.domain.SavedLocation
import com.meteo.app.domain.WeatherData
import java.lang.reflect.Type
import java.time.LocalDate
import java.time.format.DateTimeFormatter

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocationStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("meteo_locations", Context.MODE_PRIVATE)

    private val gson = GsonBuilder()
        .registerTypeAdapter(LocalDate::class.java, object : JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {
            private val formatter = DateTimeFormatter.ISO_LOCAL_DATE
            override fun serialize(src: LocalDate, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
                return JsonPrimitive(src.format(formatter))
            }
            override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): LocalDate {
                return LocalDate.parse(json.asString, formatter)
            }
        })
        .create()

    suspend fun getLocations(): List<SavedLocation> = withContext(Dispatchers.IO) {
        val json = prefs.getString("locations", null) ?: return@withContext emptyList<SavedLocation>()
        val type = object : TypeToken<List<SavedLocation>>() {}.type
        try {
            gson.fromJson(json, type) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun getHistory(): List<SavedLocation> = withContext(Dispatchers.IO) {
        val json = prefs.getString("history", null) ?: return@withContext emptyList<SavedLocation>()
        val type = object : TypeToken<List<SavedLocation>>() {}.type
        try {
            gson.fromJson(json, type) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun saveLocations(locations: List<SavedLocation>) = withContext(Dispatchers.IO) {
        val json = gson.toJson(locations)
        prefs.edit { putString("locations", json) }
    }

    private suspend fun saveHistory(history: List<SavedLocation>) = withContext(Dispatchers.IO) {
        val json = gson.toJson(history)
        prefs.edit { putString("history", json) }
    }

    suspend fun saveLastWeather(location: SavedLocation, weather: WeatherData) = withContext(Dispatchers.IO) {
        val json = gson.toJson(weather)
        prefs.edit { putString("last_weather_${location.name}", json) }
    }

    suspend fun getLastWeather(location: SavedLocation): WeatherData? = withContext(Dispatchers.IO) {
        val json = prefs.getString("last_weather_${location.name}", null) ?: return@withContext null
        try {
            val data = gson.fromJson(json, WeatherData::class.java)
            // Validation: Gson peut mettre des champs non-nullables à null s'ils manquent dans le JSON.
            // On vérifie les champs critiques pour éviter des NPE plus tard dans l'UI.
            if (data?.overview == null || data.daily5 == null) {
                null
            } else {
                data
            }
        } catch (_: Exception) {
            null
        }
    }

    suspend fun addToHistory(location: SavedLocation) = withContext(Dispatchers.IO) {
        val current = getHistory().toMutableList()
        current.removeAll { it.name == location.name }
        current.add(0, location.copy(isDefault = false))
        val limited = current.take(10)
        saveHistory(limited)
    }

    suspend fun toggleFavorite(location: SavedLocation) = withContext(Dispatchers.IO) {
        val favorites = getLocations().toMutableList()
        val exists = favorites.find { it.name == location.name }
        if (exists != null) {
            favorites.remove(exists)
        } else {
            favorites.add(location.copy(isDefault = false))
        }
        saveLocations(favorites)
    }
}
