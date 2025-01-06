package ph.edu.auf.dimarucut.jayzel.weatherapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ph.edu.auf.dimarucut.jayzel.weatherapp.model.WeatherResponse

@Composable
fun WeatherDetails(weatherResponse: WeatherResponse) {
    val tempCelsius = weatherResponse.main.temp - 273.15
    val feelsLikeCelsius = weatherResponse.main.feels_like - 273.15
    val windDirection = getWindDirection(weatherResponse.wind.deg)

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        item {
            Text(text = "UV Index: ${weatherResponse.uvIndex}", fontSize = 18.sp)
        }
        item {
            Text(text = "Humidity: ${weatherResponse.main.humidity}%", fontSize = 18.sp)
        }
        item {
            Text(text = "Feels Like: %.1f°C".format(feelsLikeCelsius), fontSize = 18.sp)
        }
        item {
            Text(text = "Wind: ${weatherResponse.wind.speed} m/s $windDirection", fontSize = 18.sp)
        }
    }
}

private fun getWindDirection(degrees: Int): String {
    return when (degrees) {
        in 0..22 -> "N"
        in 23..67 -> "NE"
        in 68..112 -> "E"
        in 113..157 -> "SE"
        in 158..202 -> "S"
        in 203..247 -> "SW"
        in 248..292 -> "W"
        in 293..337 -> "NW"
        else -> "N"
    }
}