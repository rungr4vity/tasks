package il.pacolo.com.news.data.mapper

import il.pacolo.com.news.data.remote.dto.WeatherResponse // ← data layer
import il.pacolo.com.news.domain.model.Weather              // ← domain layer


// data/mapper/WeatherMapper.kt
fun WeatherResponse.toDomain() = Weather(
    cityName = name.orEmpty(),
    temperature = main?.temp ?: 0.0,
    feelsLike = main?.feelsLike ?: 0.0,
    description = weather?.firstOrNull()?.description.orEmpty(),
    humidity = main?.humidity ?: 0L,
    windSpeed = wind?.speed ?: 0.0,
    icon = weather?.firstOrNull()?.icon.orEmpty(),
    country = sys?.country.orEmpty()
)