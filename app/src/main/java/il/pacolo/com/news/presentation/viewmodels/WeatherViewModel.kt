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


@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val repository: WeatherRepository
) : ViewModel() {

    private val _weather = MutableStateFlow<WeatherResponse?>(null)
    val weather: StateFlow<WeatherResponse?> = _weather.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun fetchByCity(city: String) {
        viewModelScope.launch {
            runCatching { repository.getWeatherByCity(city) }
                .onSuccess { _weather.value = it }
                .onFailure { _error.value = it.message }
        }
    }

    fun fetchByCoords(lat: Double, lon: Double) {
        viewModelScope.launch {
            runCatching { repository.getWeatherByCoords(lat, lon) }
                .onSuccess { _weather.value = it }
                .onFailure { _error.value = it.message }
        }
    }
}