package com.meteo.app.ui

import com.meteo.app.data.WeatherRepository
import com.meteo.app.data.api.GeocodingResult
import com.meteo.app.data.local.LocationStore
import com.meteo.app.domain.HourRow
import com.meteo.app.domain.SavedLocation
import com.meteo.app.domain.WeatherData
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class WeatherViewModelTest {

    private val repository = mockk<WeatherRepository>()
    private val locationStore = mockk<LocationStore>()
    private lateinit var viewModel: WeatherViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    private val paris = SavedLocation("Paris", 48.8566, 2.3522)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { locationStore.getLocations() } returns emptyList()
        coEvery { locationStore.getHistory() } returns emptyList()
        viewModel = WeatherViewModel(repository, locationStore)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // --- Tests for load() ---

    @Test
    fun `load succeeds when no cache exists`() = runTest {
        val mockWeather = mockk<WeatherData>()
        coEvery { locationStore.getLastWeather(paris) } returns null
        coEvery { repository.fetchWeather(paris.latitude, paris.longitude, paris.name) } returns mockWeather
        coEvery { locationStore.saveLastWeather(paris, mockWeather) } returns Unit

        viewModel.load(paris)

        val state = viewModel.state.value
        assertTrue(state is WeatherUiState.Success)
        assertEquals(mockWeather, (state as WeatherUiState.Success).data)
        assertEquals(false, state.isOffline)
        assertEquals(false, state.isRefreshing)
        coVerify { locationStore.saveLastWeather(paris, mockWeather) }
    }

    @Test
    fun `load handles failure when no cache exists`() = runTest {
        coEvery { locationStore.getLastWeather(paris) } returns null
        coEvery { repository.fetchWeather(any(), any(), any()) } throws Exception("API Error")

        viewModel.load(paris)

        assertTrue(viewModel.state.value is WeatherUiState.Error)
        assertEquals("API Error", (viewModel.state.value as WeatherUiState.Error).message)
    }

    @Test
    fun `load shows cache then updates with network success`() = runTest {
        val cachedWeather = mockk<WeatherData>()
        val networkWeather = mockk<WeatherData>()
        coEvery { locationStore.getLastWeather(paris) } returns cachedWeather
        coEvery { repository.fetchWeather(any(), any(), any()) } returns networkWeather
        coEvery { locationStore.saveLastWeather(paris, networkWeather) } returns Unit

        viewModel.load(paris)

        val state = viewModel.state.value
        assertTrue(state is WeatherUiState.Success)
        val success = state as WeatherUiState.Success
        assertEquals(networkWeather, success.data)
        assertEquals(false, success.isOffline)
    }

    @Test
    fun `load shows cache and remains offline on network failure`() = runTest {
        val cachedWeather = mockk<WeatherData>()
        coEvery { locationStore.getLastWeather(paris) } returns cachedWeather
        coEvery { repository.fetchWeather(any(), any(), any()) } throws Exception("Offline")

        viewModel.load(paris)

        val state = viewModel.state.value
        assertTrue(state is WeatherUiState.Success)
        val success = state as WeatherUiState.Success
        assertEquals(cachedWeather, success.data)
        assertTrue(success.isOffline)
        assertEquals(false, success.isRefreshing)
    }

    @Test
    fun `load updates history when addToHistory is true`() = runTest {
        val mockWeather = mockk<WeatherData>()
        coEvery { locationStore.getLastWeather(paris) } returns null
        coEvery { repository.fetchWeather(any(), any(), any()) } returns mockWeather
        coEvery { locationStore.saveLastWeather(any(), any()) } returns Unit
        coEvery { locationStore.addToHistory(paris) } returns Unit
        coEvery { locationStore.getHistory() } returns listOf(paris)

        viewModel.load(paris, addToHistory = true)

        coVerify { locationStore.addToHistory(paris) }
        assertEquals(1, viewModel.history.value.size)
        assertEquals(paris, viewModel.history.value[0])
    }

    // --- Tests for toggleFavorite() ---

    @Test
    fun `toggleFavorite updates savedLocations`() = runTest {
        coEvery { locationStore.toggleFavorite(paris) } returns Unit
        coEvery { locationStore.getLocations() } returns listOf(paris)

        viewModel.toggleFavorite(paris)

        coVerify { locationStore.toggleFavorite(paris) }
        assertEquals(1, viewModel.savedLocations.value.size)
        assertEquals(paris, viewModel.savedLocations.value[0])
    }

    // --- Tests for search() ---

    @Test
    fun `search clears results for short queries`() {
        viewModel.search("a")
        assertTrue(viewModel.searchResults.value.isEmpty())
    }

    @Test
    fun `search updates results for valid queries`() {
        val results = listOf(GeocodingResult("Paris", 48.0, 2.0, "France", "IDF"))
        coEvery { repository.searchCity("Paris") } returns results

        viewModel.search("Paris")

        assertEquals(results, viewModel.searchResults.value)
    }

    @Test
    fun `clearSearch empties results`() {
        // First fill it
        val results = listOf(GeocodingResult("Paris", 48.0, 2.0, "France", "IDF"))
        coEvery { repository.searchCity("Paris") } returns results
        viewModel.search("Paris")
        
        viewModel.clearSearch()
        
        assertTrue(viewModel.searchResults.value.isEmpty())
    }

    // --- Tests for loadHourlyForDay() ---

    @Test
    fun `loadHourlyForDay updates state to Success on repository success`() {
        val date = LocalDate.now()
        val mockHours = listOf(mockk<HourRow>())
        coEvery { repository.fetchHourlyForDay(paris.latitude, paris.longitude, date) } returns mockHours

        viewModel.loadHourlyForDay(paris, date)

        val state = viewModel.dayHourlyState.value
        assertTrue(state is DayHourlyState.Success)
        assertEquals(mockHours, (state as DayHourlyState.Success).hours)
    }

    @Test
    fun `loadHourlyForDay updates state to Error on repository failure`() {
        val date = LocalDate.now()
        coEvery { repository.fetchHourlyForDay(any(), any(), any()) } throws Exception("Failed")

        viewModel.loadHourlyForDay(paris, date)

        val state = viewModel.dayHourlyState.value
        assertTrue(state is DayHourlyState.Error)
        assertEquals("Failed", (state as DayHourlyState.Error).message)
    }

    @Test
    fun `clearDayHourly resets state to None`() {
        viewModel.clearDayHourly()
        assertEquals(DayHourlyState.None, viewModel.dayHourlyState.value)
    }
}
