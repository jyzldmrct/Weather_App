package ph.edu.auf.dimarucut.jayzel.weatherapp.model

data class WeatherResponse(
    val coord: Coord,
    val main: Main,
    val weather: List<Weather>,
    val wind: Wind,
    val name: String
)

data class Coordinates(
    val lat: Double,
    val lon: Double
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
    val description: String,
    val icon: String
)



data class HourlyForecast(
    val dt: Long,
    val main: Main,
    val weather: List<Weather>
)

data class City(
    val name: String,
    val coord: Coord
)

data class Coord(
    val lat: Double,
    val lon: Double
)

data class WeeklyForecast(
    val day: String,
    val temperature: Double,
    val weatherDescription: String
)

data class HourlyForecastResponse(
    val list: List<HourlyForecast>
)

data class WeeklyForecastResponse(
    val list: List<WeeklyForecast>
)
