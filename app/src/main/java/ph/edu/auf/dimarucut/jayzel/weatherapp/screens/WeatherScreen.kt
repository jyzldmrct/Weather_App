package ph.edu.auf.dimarucut.jayzel.weatherapp.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Geocoder
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.border
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ph.edu.auf.dimarucut.jayzel.weatherapp.R
import ph.edu.auf.dimarucut.jayzel.weatherapp.api.RetrofitInstance
import ph.edu.auf.dimarucut.jayzel.weatherapp.model.Coord
import ph.edu.auf.dimarucut.jayzel.weatherapp.model.WeatherResponse
import ph.edu.auf.dimarucut.jayzel.weatherapp.model.HourlyForecast
import ph.edu.auf.dimarucut.jayzel.weatherapp.model.Main
import ph.edu.auf.dimarucut.jayzel.weatherapp.model.Weather
import ph.edu.auf.dimarucut.jayzel.weatherapp.model.WeeklyForecast
import ph.edu.auf.dimarucut.jayzel.weatherapp.model.Wind
import java.util.*

@Composable
fun WeatherScreen(fusedLocationClient: FusedLocationProviderClient) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var weatherResponse by remember { mutableStateOf<WeatherResponse?>(null) }
    var hourlyForecast by remember { mutableStateOf<List<HourlyForecast>?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var locationDetails by remember { mutableStateOf(LocationDetails()) }
    var searchQuery by remember { mutableStateOf("") }
    var weeklyForecasts by remember { mutableStateOf<List<WeeklyForecast>?>(null) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted: Boolean ->
            if (isGranted) {
                fetchLocationAndWeather(
                    fusedLocationClient,
                    context,
                    coroutineScope,
                    onWeatherFetched = { response, details ->
                        weatherResponse = response
                        locationDetails = details
                    },
                    onHourlyForecastFetched = { hourlyForecast = it },
                    onWeeklyForecastFetched = { weeklyForecasts = it },
                    onError = { errorMessage = it }
                )
            } else {
                errorMessage = "Location permission denied"
            }
        }
    )

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fetchLocationAndWeather(
                fusedLocationClient,
                context,
                coroutineScope,
                onWeatherFetched = { response, details ->
                    weatherResponse = response
                    locationDetails = details
                },
                onHourlyForecastFetched = { hourlyForecast = it },
                onWeeklyForecastFetched = { weeklyForecasts = it },
                onError = { errorMessage = it }
            )
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SearchBar(
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            onSearch = {
                coroutineScope.launch {
                    try {
                        val response = RetrofitInstance.api.getWeatherByCityName(
                            cityName = searchQuery,
                            apiKey = "8b9290da0228ad4e99bc79358e2c70b8"
                        )
                        weatherResponse = response
                        locationDetails = LocationDetails(city = searchQuery)

                        // Fetch hourly forecast for the searched city
                        val hourlyResponse = RetrofitInstance.api.getHourlyForecast(
                            latitude = response.coord.lat.toString(),
                            longitude = response.coord.lon.toString(),
                            apiKey = "8b9290da0228ad4e99bc79358e2c70b8"
                        )
                        hourlyForecast = hourlyResponse.list
                        Log.d("WeatherScreen", "Hourly Forecast API Response: ${hourlyResponse.list}")

                        // Fetch weekly forecast for the searched city
                        val weeklyResponse = RetrofitInstance.api.getWeeklyForecast(
                            latitude = response.coord.lat.toString(),
                            longitude = response.coord.lon.toString(),
                            apiKey = "8b9290da0228ad4e99bc79358e2c70b8"
                        )
                        weeklyForecasts = weeklyResponse.list
                        Log.d("WeatherScreen", "Weekly Forecast API Response: ${weeklyResponse.list}")

                    } catch (e: Exception) {
                        errorMessage = e.message
                    }
                }
            },
            onLocationClick = {
                fetchLocationAndWeather(
                    fusedLocationClient,
                    context,
                    coroutineScope,
                    onWeatherFetched = { response, details ->
                        weatherResponse = response
                        locationDetails = details
                        searchQuery = details.city
                    },
                    onHourlyForecastFetched = { hourlyForecast = it },
                    onWeeklyForecastFetched = { weeklyForecasts = it },
                    onError = { errorMessage = it }
                )
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        WeatherInfo(weatherResponse, locationDetails, errorMessage)

        Spacer(modifier = Modifier.height(16.dp))

        HourlyForecastSection(hourlyForecasts = hourlyForecast)

        Spacer(modifier = Modifier.height(16.dp))

        weatherResponse?.let {
            RecommendationBoxes(description = it.weather[0].description)
        }

        Spacer(modifier = Modifier.height(16.dp))

        WeeklyForecastSection(weeklyForecasts)
    }
}

@SuppressLint("MissingPermission")
private fun fetchLocationAndWeather(
    fusedLocationClient: FusedLocationProviderClient,
    context: android.content.Context,
    coroutineScope: CoroutineScope,
    onWeatherFetched: (WeatherResponse, LocationDetails) -> Unit,
    onHourlyForecastFetched: (List<HourlyForecast>) -> Unit,
    onWeeklyForecastFetched: (List<WeeklyForecast>) -> Unit,
    onError: (String) -> Unit
) {
    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
        location?.let {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(it.latitude, it.longitude, 1)
            val cityName = addresses?.get(0)?.locality ?: "Unknown"
            val barangayName = addresses?.get(0)?.subLocality ?: "Unknown"
            val provinceName = addresses?.get(0)?.adminArea ?: "Unknown"

            coroutineScope.launch {
                try {
                    val response = RetrofitInstance.api.getWeather(
                        latitude = it.latitude.toString(),
                        longitude = it.longitude.toString(),
                        apiKey = "8b9290da0228ad4e99bc79358e2c70b8"
                    )
                    onWeatherFetched(response, LocationDetails(cityName, barangayName, provinceName))

                    val hourlyResponse = RetrofitInstance.api.getHourlyForecast(
                        latitude = it.latitude.toString(),
                        longitude = it.longitude.toString(),
                        apiKey = "8b9290da0228ad4e99bc79358e2c70b8"
                    )
                    onHourlyForecastFetched(hourlyResponse.list)

                    val weeklyResponse = RetrofitInstance.api.getWeeklyForecast(
                        latitude = it.latitude.toString(),
                        longitude = it.longitude.toString(),
                        apiKey = "8b9290da0228ad4e99bc79358e2c70b8"
                    )
                    onWeeklyForecastFetched(weeklyResponse.list)
                } catch (e: Exception) {
                    onError(e.message ?: "Unknown error")
                }
            }
        }
    }
}

@Composable
fun SearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onLocationClick: () -> Unit
) {
    TextField(
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        label = { Text("Search Location") },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        leadingIcon = {
            IconButton(onClick = onLocationClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_location),
                    contentDescription = "Get Current Location"
                )
            }
        },
        trailingIcon = {
            IconButton(onClick = onSearch) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_search),
                    contentDescription = "Search"
                )
            }
        }
    )
}

@Composable
fun WeatherInfo(
    weatherResponse: WeatherResponse?,
    locationDetails: LocationDetails,
    errorMessage: String?
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        weatherResponse?.let {

            val tempCelsius = it.main.temp - 273.15
            val tempFahrenheit = tempCelsius * 9 / 5 + 32

            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "%.0f°".format(tempCelsius, tempFahrenheit),
                        fontSize = 80.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Start
                    )
    
                    Text(
                        text = " ${it.weather[0].description.split(" ").joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } }}",
                        modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                    )
                }
    
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = locationDetails.city, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp)) // Add space between the texts
                    locationDetails.province?.let { Text(text = it, fontSize = 24.sp) }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
            }

        } ?: errorMessage?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun RecommendationBoxes(description: String) {
    val recommendations = getRecommendation(description).split("\n")

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Lifestyle tips",
            modifier = Modifier.padding(bottom = 8.dp)
        )

        for (i in recommendations.indices step 2) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp)
                        .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Text(text = recommendations[i])
                }
                if (i + 1 < recommendations.size) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(4.dp)
                            .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Text(text = recommendations[i + 1])
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

fun getRecommendation(description: String): String {
    return when (description.toLowerCase(Locale.ROOT)) {
        "clear sky" -> "Enjoy the sun.\nWear sunglasses.\nPerfect for a picnic.\nLight clothing recommended."
        "few clouds" -> "Carry a light jacket.\nThe sun is still visible.\nIdeal for a walk.\nLight layers work well."
        "scattered clouds", "broken clouds" -> "A bit cloudy.\nJacket might be needed.\nIdeal for a casual day out.\nComfortable temperatures."
        "overcast clouds" -> "Overcast skies.\nMight rain later.\nKeep an umbrella handy.\nDress in layers."
        "shower rain", "rain" -> "It's raining.\nBring an umbrella.\nWaterproof shoes recommended.\nStay dry!"
        "thunderstorm" -> "Thunderstorm ahead.\nStay indoors.\nAvoid outdoor activities.\nStay safe, avoid electrical devices."
        "snow" -> "Snowing.\nWear warm clothing.\nBe careful on the roads.\nGreat day for a snowball fight!"
        "mist", "fog" -> "Misty conditions.\nDrive with caution.\nKeep warm in layers.\nVisibility is low."
        else -> "Weather is unpredictable.\nStay prepared.\nKeep an eye on the forecast.\nBe flexible with plans!"
    }
}

data class LocationDetails(
    val city: String = "Unknown",
    val barangay: String? = null,
    val province: String? = null
)

fun getWeatherIconResId(description: String): Int {
    return when (description.toLowerCase(Locale.ROOT)) {
        "clear sky" -> R.drawable.ic_sunny
        "few clouds" -> R.drawable.ic_partly_cloudy
        "scattered clouds", "broken clouds" -> R.drawable.ic_cloudy
        "overcast clouds" -> R.drawable.ic_overcast
        "shower rain", "rain" -> R.drawable.ic_rain
        "thunderstorm" -> R.drawable.ic_thunderstorm
        "snow" -> R.drawable.ic_snow
        "mist", "fog" -> R.drawable.ic_fog
        else -> R.drawable.ic_unknown
    }
}

@Preview(showBackground = true)
@Composable
fun WeatherInfoPreview() {
    val sampleWeatherResponse = WeatherResponse(
        coord = Coord(lat = 14.5995, lon = 120.9842),
        main = Main(temp = 298.15, humidity = 80),
        weather = listOf(Weather(description = "clear sky", icon = "01d")),
        wind = Wind(speed = 5.0),
        name = "Manila"
    )
    val sampleLocationDetails = LocationDetails(
        city = "Manila",
        barangay = "Barangay 123",
        province = "Metro Manila"
    )
    val sampleErrorMessage: String? = null

    MaterialTheme {
        WeatherInfo(
            weatherResponse = sampleWeatherResponse,
            locationDetails = sampleLocationDetails,
            errorMessage = sampleErrorMessage
        )
    }
}