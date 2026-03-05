package il.pacolo.com.news.domain.model

// domain/model/Weather.kt
data class Weather(
    val cityName: String,
    val temperature: Double,
    val feelsLike: Double,
    val description: String,
    val humidity: Long,
    val windSpeed: Double,
    val icon: String,
    val country: String
    // only what your UI actually needs
)