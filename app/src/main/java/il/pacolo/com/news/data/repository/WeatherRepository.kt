package il.pacolo.com.news.data.repository

import il.pacolo.com.news.data.remote.ApiService
import il.pacolo.com.news.data.remote.dto.WeatherResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepository @Inject constructor(
    private val api: ApiService,
    private val apiKey: String          // <-- injected from NativeKeys via Hilt
) {
    suspend fun getWeatherByCity(city: String): WeatherResponse =
        api.getLocation(q = city, appid = apiKey)

    suspend fun getWeatherByCoords(lat: Double, lon: Double): WeatherResponse =
        api.getLocationLatLong(lat = lat, lon = lon, appid = apiKey)
}