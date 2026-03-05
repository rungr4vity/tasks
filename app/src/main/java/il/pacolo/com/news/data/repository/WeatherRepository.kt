package il.pacolo.com.news.data.repository

import il.pacolo.com.news.data.remote.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepository @Inject constructor(
    private val api: ApiService,
    private val apiKey: String
) {
    suspend fun getWeatherByCity(city: String) =
        api.getLocation(q = city, appid = apiKey)

    suspend fun getWeatherByCoords(lat: Double, lon: Double) =
        api.getLocationLatLong(lat = lat, lon = lon, appid = apiKey)
}