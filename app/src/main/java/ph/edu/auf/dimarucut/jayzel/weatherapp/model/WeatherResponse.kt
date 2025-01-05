package ph.edu.auf.dimarucut.jayzel.weatherapp.model

data class WeatherResponse(
    val main: Main,
    val weather: List<Weather>,
    val wind: Wind,
    val name: String
)

data class Main(
    val temp: Double,
    val humidity: Int
)

data class Weather(
    val description: String,
    val icon: String
)

data class Wind(
    val speed: Double
)

data class WeeklyForecastResponse(
    val daily: List<DailyForecast>
)

data class DailyForecast(
    val dt: Long,
    val temp: Temperature,
    val weather: List<WeatherCondition>
)

data class Temperature(
    val day: Double,
    val min: Double,
    val max: Double,
    val night: Double,
    val eve: Double,
    val morn: Double
)

data class WeatherCondition(
    val id: Int,
    val main: String,
    val description: String,
    val icon: String
)
