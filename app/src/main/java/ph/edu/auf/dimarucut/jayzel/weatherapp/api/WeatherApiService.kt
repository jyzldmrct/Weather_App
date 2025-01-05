package ph.edu.auf.dimarucut.jayzel.weatherapp.api

import ph.edu.auf.dimarucut.jayzel.weatherapp.model.WeatherResponse
import ph.edu.auf.dimarucut.jayzel.weatherapp.model.WeeklyForecastResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    @GET("weather")
    suspend fun getWeather(
        @Query("lat") latitude: String,
        @Query("lon") longitude: String,
        @Query("apiKey") apiKey: String
    ): WeatherResponse

    @GET("forecast/daily")
    suspend fun getWeeklyForecast(
        @Query("lat") latitude: String,
        @Query("lon") longitude: String,
        @Query("apiKey") apiKey: String
    ): List<WeatherResponse>

    @GET("weather")
    suspend fun getWeatherByCityName(
        @Query("q") cityName: String,
        @Query("apiKey") apiKey: String
    ): WeatherResponse

    @GET("forecast/daily")
    suspend fun getWeeklyForecastByCityName(
        @Query("q") cityName: String,
        @Query("apiKey") apiKey: String
    ): List<WeatherResponse>

    @GET("onecall")
    suspend fun getWeeklyForecast(
        @Query("lat") latitude: String,
        @Query("lon") longitude: String,
        @Query("exclude") exclude: String = "current,minutely,hourly,alerts",
        @Query("units") units: String = "metric", // Optional: Fetch in Celsius
        @Query("appid") apiKey: String
    ): WeeklyForecastResponse

}