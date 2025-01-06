package ph.edu.auf.dimarucut.jayzel.weatherapp.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import ph.edu.auf.dimarucut.jayzel.weatherapp.model.WeeklyForecast

@Composable
fun WeeklyForecastSection(
    weeklyForecasts: List<WeeklyForecast>?,
    modifier: Modifier = Modifier
) {
    if (weeklyForecasts == null || weeklyForecasts.isEmpty()) {
        Text("No forecast data available.")
        return
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Weekly Forecast",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Card(
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                weeklyForecasts.forEach { forecast ->
                    WeeklyForecastItem(forecast)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun WeeklyForecastItem(forecast: WeeklyForecast) {
    val formattedTemperature = "%.2f".format(forecast.temperature)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = forecast.day,
            style = MaterialTheme.typography.bodyLarge
        )

        Image(
            painter = painterResource(
                id = getWeatherIconResId(forecast.weatherDescription)
            ),
            contentDescription = forecast.weatherDescription,
            modifier = Modifier.size(40.dp)
        )

        Text(
            text = "$formattedTemperature°C",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}