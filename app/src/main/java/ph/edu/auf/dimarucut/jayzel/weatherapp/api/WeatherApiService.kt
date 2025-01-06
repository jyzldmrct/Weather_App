package ph.edu.auf.dimarucut.jayzel.weatherapp.api

import ph.edu.auf.dimarucut.jayzel.weatherapp.model.HourlyForecastResponse
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

    @GET("weather")
    suspend fun getWeatherByCityName(
        @Query("q") cityName: String,
        @Query("apiKey") apiKey: String
    ): WeatherResponse


    @GET("forecast")
    suspend fun getHourlyForecast(
        @Query("lat") latitude: String,
        @Query("lon") longitude: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): HourlyForecastResponse

}