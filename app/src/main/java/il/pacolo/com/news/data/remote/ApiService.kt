package il.pacolo.com.news.data.remote

import il.pacolo.com.news.data.remote.dto.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("weather?&units=imperial")
    suspend fun getLocation(
        @Query("q") q: String,
        @Query("appid") appid: String
    ): WeatherResponse

    @GET("weather?&units=imperial")
    suspend fun getLocationLatLong(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") appid: String
    ): WeatherResponse

////    @GET("/api/character/")
////    suspend fun getCharacters(@Query("page") page: Int): ResponseWrapper
//
//    @GET("weather?&units=imperial&appid=97fb0baad55faa308a3ff5ccc0a2d19e")
//    suspend fun getLocation(
//        @Query("q") q: String
//    ): WeatherResponse
//
//
//
//    @GET("weather?&units=imperial&appid=97fb0baad55faa308a3ff5ccc0a2d19e")
//    suspend fun getLocationLatLong(
//        @Query("lat") lat: Double,
//        @Query("lon") lon: Double
//    ): WeatherResponse

}