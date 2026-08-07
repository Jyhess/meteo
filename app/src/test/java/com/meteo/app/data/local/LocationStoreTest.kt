package com.meteo.app.data.local

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.meteo.app.domain.SavedLocation
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

import kotlinx.coroutines.test.runTest

class LocationStoreTest {

    private lateinit var context: Context
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var locationStore: LocationStore

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        sharedPreferences = mockk(relaxed = true)
        every { context.applicationContext.getSharedPreferences("meteo_locations", Context.MODE_PRIVATE) } returns sharedPreferences
        locationStore = LocationStore(context)
    }

    @Test
    fun `getLocations returns empty list when json is null`() = runTest {
        every { sharedPreferences.getString("locations", null) } returns null
        val result = locationStore.getLocations()
        assertTrue(result.isEmpty())
    }

    @Test
    fun `getLocations returns empty list when json is invalid`() = runTest {
        every { sharedPreferences.getString("locations", null) } returns "invalid json"
        val result = locationStore.getLocations()
        assertTrue(result.isEmpty())
    }

    @Test
    fun `getHistory returns empty list when json is null`() = runTest {
        every { sharedPreferences.getString("history", null) } returns null
        val result = locationStore.getHistory()
        assertTrue(result.isEmpty())
    }

    @Test
    fun `getHistory returns empty list when json is invalid`() = runTest {
        every { sharedPreferences.getString("history", null) } returns "{ corrupt: data }"
        val result = locationStore.getHistory()
        assertTrue(result.isEmpty())
    }

    @Test
    fun `getLocations returns list when json is valid`() = runTest {
        val json = """[{"name":"Paris","latitude":48.85,"longitude":2.35,"isDefault":false}]"""
        every { sharedPreferences.getString("locations", null) } returns json
        val result = locationStore.getLocations()
        assertEquals(1, result.size)
        assertEquals("Paris", result[0].name)
    }

    @Test
    fun `addToHistory adds location to front and limits size`() = runTest {
        val location1 = SavedLocation("Paris", 48.85, 2.35)
        
        // Mocking existing history (10 items)
        val existingItems = (1..10).map { SavedLocation("City$it", 0.0, 0.0) }
        val json = Gson().toJson(existingItems)
        every { sharedPreferences.getString("history", null) } returns json
        
        val editor = mockk<SharedPreferences.Editor>(relaxed = true)
        every { sharedPreferences.edit() } returns editor

        locationStore.addToHistory(location1)
        
        // Capture what was saved to check content and size
        val capturedJson = slot<String>()
        verify { editor.putString("history", capture(capturedJson)) }
        
        val savedList: List<SavedLocation> = Gson().fromJson(
            capturedJson.captured, 
            object : TypeToken<List<SavedLocation>>() {}.type
        )
        
        assertEquals(10, savedList.size)
        assertEquals("Paris", savedList[0].name)
    }

    @Test
    fun `toggleFavorite adds or removes location`() = runTest {
        val location = SavedLocation("Paris", 48.85, 2.35)
        
        // Case 1: Add to empty favorites
        every { sharedPreferences.getString("locations", null) } returns null
        val editor = mockk<SharedPreferences.Editor>(relaxed = true)
        every { sharedPreferences.edit() } returns editor
        
        locationStore.toggleFavorite(location)
        verify { editor.putString("locations", any()) }

        // Case 2: Remove from favorites
        val json = """[{"name":"Paris","latitude":48.85,"longitude":2.35,"isDefault":false}]"""
        every { sharedPreferences.getString("locations", null) } returns json
        
        locationStore.toggleFavorite(location)
        verify { editor.putString("locations", "[]") }
    }

    @Test
    fun `saveLastWeather and getLastWeather handle invalid data correctly`() = runTest {
        val location = SavedLocation("Paris", 48.85, 2.35)
        
        val editor = mockk<SharedPreferences.Editor>(relaxed = true)
        every { sharedPreferences.edit() } returns editor
        
        // Mocking corrupt weather data (invalid JSON)
        every { sharedPreferences.getString("last_weather_Paris", null) } returns "{ corrupt }" 
        
        val result = locationStore.getLastWeather(location)
        assertNull(result)
    }

    @Test
    fun `getLastWeather returns null when critical fields are missing in json`() = runTest {
        val location = SavedLocation("Paris", 48.85, 2.35)
        
        // JSON missing 'overview' and 'daily5'
        val incompleteJson = """{"locationLabel":"Paris"}"""
        every { sharedPreferences.getString("last_weather_Paris", null) } returns incompleteJson
        
        val result = locationStore.getLastWeather(location)
        assertNull(result)
    }
}
