package com.meteo.app.data.local

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.meteo.app.domain.SavedLocation

class LocationStore(context: Context) {
    private val prefs = context.getSharedPreferences("meteo_locations", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun getLocations(): List<SavedLocation> {
        val json = prefs.getString("locations", null) ?: return emptyList()
        val type = object : TypeToken<List<SavedLocation>>() {}.type
        return gson.fromJson(json, type)
    }

    fun getHistory(): List<SavedLocation> {
        val json = prefs.getString("history", null) ?: return emptyList()
        val type = object : TypeToken<List<SavedLocation>>() {}.type
        return gson.fromJson(json, type)
    }

    fun saveLocations(locations: List<SavedLocation>) {
        val json = gson.toJson(locations)
        prefs.edit().putString("locations", json).apply()
    }

    private fun saveHistory(history: List<SavedLocation>) {
        val json = gson.toJson(history)
        prefs.edit().putString("history", json).apply()
    }

    fun addToHistory(location: SavedLocation) {
        val current = getHistory().toMutableList()
        current.removeAll { it.name == location.name }
        current.add(0, location.copy(isDefault = false))
        val limited = current.take(10)
        saveHistory(limited)
    }

    fun toggleFavorite(location: SavedLocation) {
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
