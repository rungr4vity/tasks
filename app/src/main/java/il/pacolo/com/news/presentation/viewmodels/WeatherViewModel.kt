package il.pacolo.com.news.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import il.pacolo.com.news.data.remote.dto.WeatherResponse
import il.pacolo.com.news.data.repository.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import il.pacolo.com.news.data.local.LocationHelper
import il.pacolo.com.news.data.local.WeatherPreferences
import kotlinx.coroutines.flow.*


@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val repository: WeatherRepository,
    private val preferences: WeatherPreferences,
    private val locationHelper: LocationHelper
) : ViewModel() {

    private val _weather = MutableStateFlow<WeatherResponse?>(null)
    val weather: StateFlow<WeatherResponse?> = _weather.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val lastCity: StateFlow<String?> = preferences.lastCity
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Called once on launch — loads last city if no location permission
    fun loadLastCity() {
        viewModelScope.launch {
            preferences.lastCity.firstOrNull()?.let { city ->
                if (city.isNotBlank()) fetchByCity(city)
            }
        }
    }

    fun fetchByCity(city: String) {
        viewModelScope.launch {
            _isLoading.value = true
            runCatching { repository.getWeatherByCity(city) }
                .onSuccess {
                    _weather.value = it
                    _error.value = null
                    preferences.saveLastCity(city)   // ← persist last city
                }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    fun fetchByCoords(lat: Double, lon: Double) {
        viewModelScope.launch {
            _isLoading.value = true
            runCatching { repository.getWeatherByCoords(lat, lon) }
                .onSuccess {
                    _weather.value = it
                    _error.value = null
                }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    // Called when location permission is granted
    fun fetchByCurrentLocation() {
        viewModelScope.launch {
            _isLoading.value = true
            runCatching { locationHelper.getCurrentLocation() }
                .onSuccess { (lat, lon) -> fetchByCoords(lat, lon) }
                .onFailure { _error.value = "Could not get location: ${it.message}" }
            _isLoading.value = false
        }
    }
}