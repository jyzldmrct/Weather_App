package ph.edu.auf.dimarucut.jayzel.weatherapp.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import kotlinx.coroutines.launch
import ph.edu.auf.dimarucut.jayzel.weatherapp.R
import ph.edu.auf.dimarucut.jayzel.weatherapp.api.RetrofitInstance
import java.util.*
import ph.edu.auf.dimarucut.jayzel.weatherapp.model.WeatherResponse
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.tooling.preview.Preview
import com.google.android.gms.location.LocationServices


@Composable
fun WeatherScreen(fusedLocationClient: FusedLocationProviderClient) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var weatherResponse by remember { mutableStateOf<WeatherResponse?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var cityName by remember { mutableStateOf<String?>(null) }
    var barangayName by remember { mutableStateOf<String?>(null) }
    var provinceName by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted: Boolean ->
            if (isGranted) {
                getLastLocation(fusedLocationClient, context, onLocationReceived = { lat, lon, city, barangay, province ->
                    coroutineScope.launch {
                        try {
                            val response = RetrofitInstance.api.getWeather(
                                latitude = lat.toString(),
                                longitude = lon.toString(),
                                apiKey = "8b9290da0228ad4e99bc79358e2c70b8"
                            )
                            weatherResponse = response
                            cityName = city
                            barangayName = barangay
                            provinceName = province
                        } catch (e: Exception) {
                            errorMessage = e.message
                        }
                    }
                })
            } else {
                errorMessage = "Location permission denied"
            }
        }
    )

    LaunchedEffect(Unit) {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) -> {
                getLastLocation(fusedLocationClient, context, onLocationReceived = { lat, lon, city, barangay, province ->
                    coroutineScope.launch {
                        try {
                            val response = RetrofitInstance.api.getWeather(
                                latitude = lat.toString(),
                                longitude = lon.toString(),
                                apiKey = "8b9290da0228ad4e99bc79358e2c70b8"
                            )
                            weatherResponse = response
                            cityName = city
                            barangayName = barangay
                            provinceName = province
                        } catch (e: Exception) {
                            errorMessage = e.message
                        }
                    }
                })
            }
            else -> {
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

    TextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        label = { Text("Search Location") },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        leadingIcon = {
            IconButton(onClick = {
                coroutineScope.launch {
                    getLastLocation(fusedLocationClient, context, onLocationReceived = { lat, lon, city, barangay, province ->
                        coroutineScope.launch {
                            try {
                                val response = RetrofitInstance.api.getWeather(
                                    latitude = lat.toString(),
                                    longitude = lon.toString(),
                                    apiKey = "8b9290da0228ad4e99bc79358e2c70b8"
                                )
                                weatherResponse = response
                                cityName = city
                                barangayName = barangay
                                provinceName = province
                                searchQuery = city
                            } catch (e: Exception) {
                                errorMessage = e.message
                            }
                        }
                    })
                }
            }) {
                Icon(painter = painterResource(id = R.drawable.ic_location), contentDescription = "Get Current Location")
            }
        },
        trailingIcon = {
            IconButton(onClick = {
                coroutineScope.launch {
                    try {
                        val response = RetrofitInstance.api.getWeatherByCityName(
                            cityName = searchQuery,
                            apiKey = "8b9290da0228ad4e99bc79358e2c70b8"
                        )
                        weatherResponse = response
                        cityName = searchQuery
                        barangayName = null
                        provinceName = null
                    } catch (e: Exception) {
                        errorMessage = e.message
                    }
                }
            }) {
                Icon(painter = painterResource(id = R.drawable.ic_search), contentDescription = "Search")
            }
        }
    )
        Spacer(modifier = Modifier.height(16.dp))
        Box(
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                weatherResponse?.let {
                    val weatherIconResId = getWeatherIconResId(it.weather[0].description)
                    Image(
                        painter = painterResource(id = weatherIconResId),
                        contentDescription = "Weather Icon",
                        modifier = Modifier.size(128.dp)
                    )
                }
                cityName?.let {
                    Text(
                        text = it,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        barangayName?.let {
            Text(text = "Barangay: $it")
        }
        provinceName?.let {
            Text(text = "Province: $it")
        }
        weatherResponse?.let {
            val tempCelsius = it.main.temp - 273.15
            val tempFahrenheit = tempCelsius * 9/5 + 32
            Text(text = "Temperature: %.2f°C / %.2f°F".format(tempCelsius, tempFahrenheit))
            Text(text = "Weather: ${it.weather[0].description}")
        } ?: errorMessage?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }
    }
}

@SuppressLint("MissingPermission")
private fun getLastLocation(
    fusedLocationClient: FusedLocationProviderClient,
    context: android.content.Context,
    onLocationReceived: (Double, Double, String, String, String) -> Unit
) {
    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
        location?.let {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(it.latitude, it.longitude, 1)
            val cityName = addresses?.get(0)?.locality ?: "Unknown"
            val barangayName = addresses?.get(0)?.subLocality ?: "Unknown"
            val provinceName = addresses?.get(0)?.adminArea ?: "Unknown"
            onLocationReceived(it.latitude, it.longitude, cityName, barangayName, provinceName)
        }
    }
}

private fun getWeatherIconResId(description: String): Int {
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
fun WeatherScreenPreview() {
    val context = LocalContext.current
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    WeatherScreen(fusedLocationClient = fusedLocationClient)
}