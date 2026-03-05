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

@OptIn(ExperimentalCoroutinesApi::class)
class WeatherViewModelTest {

    @MockK
    private lateinit var repository: WeatherRepository

    private lateinit var viewModel: WeatherViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    // Fake response
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
        viewModel = WeatherViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ─── fetchByCity ──────────────────────────────────────────────

    @Test
    fun `fetchByCity success - weather state updated`() = runTest {
        coEvery { repository.getWeatherByCity("New York") } returns fakeWeather

        viewModel.fetchByCity("New York")

        assertEquals(fakeWeather, viewModel.weather.value)
        assertNull(viewModel.error.value)
    }

    @Test
    fun `fetchByCity failure - error state updated`() = runTest {
        coEvery { repository.getWeatherByCity("Unknown") } throws Exception("City not found")

        viewModel.fetchByCity("Unknown")

        assertNull(viewModel.weather.value)
        assertEquals("City not found", viewModel.error.value)
    }

    @Test
    fun `fetchByCity called with correct city`() = runTest {
        coEvery { repository.getWeatherByCity(any()) } returns fakeWeather

        viewModel.fetchByCity("Chicago")

        coVerify(exactly = 1) { repository.getWeatherByCity("Chicago") }
    }

    @Test
    fun `fetchByCity empty city - still calls repository`() = runTest {
        coEvery { repository.getWeatherByCity("") } throws Exception("Invalid city")

        viewModel.fetchByCity("")

        assertEquals("Invalid city", viewModel.error.value)
    }

    // ─── fetchByCoords ────────────────────────────────────────────

    @Test
    fun `fetchByCoords success - weather state updated`() = runTest {
        coEvery { repository.getWeatherByCoords(40.71, -74.00) } returns fakeWeather

        viewModel.fetchByCoords(40.71, -74.00)

        assertEquals(fakeWeather, viewModel.weather.value)
        assertNull(viewModel.error.value)
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

    // ─── Initial State ────────────────────────────────────────────

    @Test
    fun `initial weather state is null`() {
        assertNull(viewModel.weather.value)
    }

    @Test
    fun `initial error state is null`() {
        assertNull(viewModel.error.value)
    }

    // ─── State overwrite ─────────────────────────────────────────

    @Test
    fun `second fetchByCity overwrites first result`() = runTest {
        val secondWeather = fakeWeather.copy(name = "Los Angeles")
        coEvery { repository.getWeatherByCity("New York") } returns fakeWeather
        coEvery { repository.getWeatherByCity("Los Angeles") } returns secondWeather

        viewModel.fetchByCity("New York")
        viewModel.fetchByCity("Los Angeles")

        assertEquals("Los Angeles", viewModel.weather.value?.name)
    }
}