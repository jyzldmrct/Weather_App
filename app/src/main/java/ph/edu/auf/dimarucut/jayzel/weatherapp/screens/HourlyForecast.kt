package ph.edu.auf.dimarucut.jayzel.weatherapp.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ph.edu.auf.dimarucut.jayzel.weatherapp.model.HourlyForecast
import ph.edu.auf.dimarucut.jayzel.weatherapp.model.Weather
import ph.edu.auf.dimarucut.jayzel.weatherapp.model.WeatherCondition
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt


@Composable
fun HourlyForecastSection(
    hourlyForecasts: List<HourlyForecast>?,
    modifier: Modifier = Modifier
) {
    if (hourlyForecasts == null || hourlyForecasts.isEmpty()) {
        Text("No forecast data available.")
        return
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(
                text = "Hourly Forecast",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 8.dp)
            ) {
                hourlyForecasts.take(24).forEach { forecast ->
                    HourlyForecastItem(forecast)
                    Spacer(modifier = Modifier.width(16.dp))
                }
            }
        }
    }
}

@Composable
private fun HourlyForecastItem(forecast: HourlyForecast) {
    val temperatureCelsius = forecast.main.temp
    val formattedTemperature = "%.2f".format(temperatureCelsius)
    Log.d("HourlyForecastItem", "Temperature (Celsius): $formattedTemperature")

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(64.dp)
    ) {
        Text(
            text = formatHour(forecast.dt),
            style = MaterialTheme.typography.bodyMedium
        )

        Image(
            painter = painterResource(
                id = getWeatherIconResId(forecast.weather.firstOrNull()?.description ?: "")
            ),
            contentDescription = forecast.weather.firstOrNull()?.description,
            modifier = Modifier.size(40.dp)
        )

        Text(
            text = "$formattedTemperature°C",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}


private fun formatHour(timestamp: Long): String {
    return try {
        SimpleDateFormat("HH:00", Locale.getDefault()).format(Date(timestamp * 1000L))
    } catch (e: Exception) {
        Log.e("formatHour", "Invalid timestamp: $timestamp", e)
        "--:--"
    }
}


