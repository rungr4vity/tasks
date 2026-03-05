package il.pacolo.com.news.presentation.viewmodels

import il.pacolo.com.news.data.remote.dto.*
import il.pacolo.com.news.data.repository.WeatherRepository
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import il.pacolo.com.news.data.local.LocationHelper
import il.pacolo.com.news.data.local.WeatherPreferences
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalCoroutinesApi::class)
class WeatherViewModelTest {

    @MockK lateinit var repository: WeatherRepository
    @MockK lateinit var preferences: WeatherPreferences
    @MockK lateinit var locationHelper: LocationHelper

    private lateinit var viewModel: WeatherViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    private val fakeWeather = WeatherResponse(
        name = "New York",
        sys = Sys(country = "US"),
        main = Main(temp = 72.0, feelsLike = 68.0, humidity = 60),
        weather = listOf(Weather(description = "clear sky")),
        wind = Wind(speed = 5.0)
    )

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)

        // Default stubs — override per test when needed
        every { preferences.lastCity } returns flowOf(null)
        coEvery { preferences.saveLastCity(any()) } just Runs

        viewModel = WeatherViewModel(repository, preferences, locationHelper)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ─── Initial State ────────────────────────────────────────────

    @Test
    fun `initial weather state is null`() {
        assertNull(viewModel.weather.value)
    }

    @Test
    fun `initial error state is null`() {
        assertNull(viewModel.error.value)
    }

    @Test
    fun `initial loading state is false`() {
        assertFalse(viewModel.isLoading.value)
    }

    // ─── fetchByCity ──────────────────────────────────────────────

    @Test
    fun `fetchByCity success - weather state updated`() = runTest {
        coEvery { repository.getWeatherByCity("New York") } returns fakeWeather

        viewModel.fetchByCity("New York")

        assertEquals(fakeWeather, viewModel.weather.value)
        assertNull(viewModel.error.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `fetchByCity success - saves last city to preferences`() = runTest {
        coEvery { repository.getWeatherByCity("New York") } returns fakeWeather

        viewModel.fetchByCity("New York")

        coVerify(exactly = 1) { preferences.saveLastCity("New York") }
    }

    @Test
    fun `fetchByCity failure - error state updated`() = runTest {
        coEvery { repository.getWeatherByCity("Unknown") } throws Exception("City not found")

        viewModel.fetchByCity("Unknown")

        assertNull(viewModel.weather.value)
        assertEquals("City not found", viewModel.error.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `fetchByCity failure - does not save to preferences`() = runTest {
        coEvery { repository.getWeatherByCity(any()) } throws Exception("Error")

        viewModel.fetchByCity("Unknown")

        coVerify(exactly = 0) { preferences.saveLastCity(any()) }
    }

    @Test
    fun `fetchByCity called with correct city`() = runTest {
        coEvery { repository.getWeatherByCity(any()) } returns fakeWeather

        viewModel.fetchByCity("Chicago")

        coVerify(exactly = 1) { repository.getWeatherByCity("Chicago") }
    }

    @Test
    fun `second fetchByCity overwrites first result`() = runTest {
        val secondWeather = fakeWeather.copy(name = "Los Angeles")
        coEvery { repository.getWeatherByCity("New York") } returns fakeWeather
        coEvery { repository.getWeatherByCity("Los Angeles") } returns secondWeather

        viewModel.fetchByCity("New York")
        viewModel.fetchByCity("Los Angeles")

        assertEquals("Los Angeles", viewModel.weather.value?.name)
    }

    // ─── fetchByCoords ────────────────────────────────────────────

    @Test
    fun `fetchByCoords success - weather state updated`() = runTest {
        coEvery { repository.getWeatherByCoords(40.71, -74.00) } returns fakeWeather

        viewModel.fetchByCoords(40.71, -74.00)

        assertEquals(fakeWeather, viewModel.weather.value)
        assertNull(viewModel.error.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `fetchByCoords failure - error state updated`() = runTest {
        coEvery { repository.getWeatherByCoords(any(), any()) } throws Exception("Location error")

        viewModel.fetchByCoords(0.0, 0.0)

        assertNull(viewModel.weather.value)
        assertEquals("Location error", viewModel.error.value)
    }

    @Test
    fun `fetchByCoords called with correct coordinates`() = runTest {
        coEvery { repository.getWeatherByCoords(any(), any()) } returns fakeWeather

        viewModel.fetchByCoords(40.71, -74.00)

        coVerify(exactly = 1) { repository.getWeatherByCoords(40.71, -74.00) }
    }

    // ─── fetchByCurrentLocation ───────────────────────────────────

    @Test
    fun `fetchByCurrentLocation success - fetches weather by coords`() = runTest {
        coEvery { locationHelper.getCurrentLocation() } returns Pair(40.71, -74.00)
        coEvery { repository.getWeatherByCoords(40.71, -74.00) } returns fakeWeather

        viewModel.fetchByCurrentLocation()

        assertEquals(fakeWeather, viewModel.weather.value)
        assertNull(viewModel.error.value)
    }

    @Test
    fun `fetchByCurrentLocation failure - error state updated`() = runTest {
        coEvery { locationHelper.getCurrentLocation() } throws Exception("GPS unavailable")

        viewModel.fetchByCurrentLocation()

        assertNull(viewModel.weather.value)
        assertTrue(viewModel.error.value!!.contains("GPS unavailable"))
    }

    // ─── loadLastCity ─────────────────────────────────────────────

    @Test
    fun `loadLastCity with saved city - fetches weather`() = runTest {
        every { preferences.lastCity } returns flowOf("Miami")
        coEvery { repository.getWeatherByCity("Miami") } returns fakeWeather

        // Recreate ViewModel so lastCity flow is picked up
        viewModel = WeatherViewModel(repository, preferences, locationHelper)
        viewModel.loadLastCity()

        assertEquals(fakeWeather, viewModel.weather.value)
    }

    @Test
    fun `loadLastCity with null - does not fetch weather`() = runTest {
        every { preferences.lastCity } returns flowOf(null)

        viewModel.loadLastCity()

        assertNull(viewModel.weather.value)
        coVerify(exactly = 0) { repository.getWeatherByCity(any()) }
    }

    @Test
    fun `loadLastCity with blank city - does not fetch weather`() = runTest {
        every { preferences.lastCity } returns flowOf("   ")

        viewModel.loadLastCity()

        assertNull(viewModel.weather.value)
        coVerify(exactly = 0) { repository.getWeatherByCity(any()) }
    }
}