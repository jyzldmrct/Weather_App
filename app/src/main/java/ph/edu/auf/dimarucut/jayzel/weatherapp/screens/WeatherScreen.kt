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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ph.edu.auf.dimarucut.jayzel.weatherapp.R
import ph.edu.auf.dimarucut.jayzel.weatherapp.api.RetrofitInstance
import ph.edu.auf.dimarucut.jayzel.weatherapp.model.WeatherResponse
import java.util.*

@Composable
fun WeatherScreen(fusedLocationClient: FusedLocationProviderClient) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var weatherResponse by remember { mutableStateOf<WeatherResponse?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var locationDetails by remember { mutableStateOf(LocationDetails()) }
    var searchQuery by remember { mutableStateOf("") }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted: Boolean ->
            if (isGranted) {
                fetchLocationAndWeather(fusedLocationClient, context, coroutineScope, onWeatherFetched = { response, details ->
                    weatherResponse = response
                    locationDetails = details
                }, onError = { errorMessage = it })
            } else {
                errorMessage = "Location permission denied"
            }
        }
    )

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fetchLocationAndWeather(fusedLocationClient, context, coroutineScope, onWeatherFetched = { response, details ->
                weatherResponse = response
                locationDetails = details
            }, onError = { errorMessage = it })
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
        SearchBar(searchQuery, onSearchQueryChange = { searchQuery = it }, onSearch = {
            coroutineScope.launch {
                try {
                    val response = RetrofitInstance.api.getWeatherByCityName(
                        cityName = searchQuery,
                        apiKey = "8b9290da0228ad4e99bc79358e2c70b8"
                    )
                    weatherResponse = response
                    locationDetails = LocationDetails(city = searchQuery)
                } catch (e: Exception) {
                    errorMessage = e.message
                }
            }
        }, onLocationClick = {
            fetchLocationAndWeather(fusedLocationClient, context, coroutineScope, onWeatherFetched = { response, details ->
                weatherResponse = response
                locationDetails = details
                searchQuery = details.city
            }, onError = { errorMessage = it })
        })

        Spacer(modifier = Modifier.height(16.dp))

        WeatherInfo(weatherResponse, locationDetails, errorMessage)
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
                Icon(painter = painterResource(id = R.drawable.ic_location), contentDescription = "Get Current Location")
            }
        },
        trailingIcon = {
            IconButton(onClick = onSearch) {
                Icon(painter = painterResource(id = R.drawable.ic_search), contentDescription = "Search")
            }
        }
    )
}

@Composable
fun WeatherInfo(weatherResponse: WeatherResponse?, locationDetails: LocationDetails, errorMessage: String?) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        weatherResponse?.let {
            val weatherIconResId = getWeatherIconResId(it.weather[0].description)
            Image(
                painter = painterResource(id = weatherIconResId),
                contentDescription = "Weather Icon",
                modifier = Modifier.size(128.dp)
            )
            Text(text = locationDetails.city, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            locationDetails.barangay?.let { Text(text = "Barangay: $it") }
            locationDetails.province?.let { Text(text = "Province: $it") }
            val tempCelsius = it.main.temp - 273.15
            val tempFahrenheit = tempCelsius * 9 / 5 + 32
            Text(text = "Temperature: %.2f°C / %.2f°F".format(tempCelsius, tempFahrenheit))
            Text(text = "Weather: ${it.weather[0].description}")
        } ?: errorMessage?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }
    }
}

@SuppressLint("MissingPermission")
private fun fetchLocationAndWeather(
    fusedLocationClient: FusedLocationProviderClient,
    context: android.content.Context,
    coroutineScope: CoroutineScope,
    onWeatherFetched: (WeatherResponse, LocationDetails) -> Unit,
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
                } catch (e: Exception) {
                    onError(e.message ?: "Unknown error")
                }
            }
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

data class LocationDetails(
    val city: String = "Unknown",
    val barangay: String? = null,
    val province: String? = null
)

@Preview(showBackground = true)
@Composable
fun WeatherScreenPreview() {
    val context = LocalContext.current
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    WeatherScreen(fusedLocationClient = fusedLocationClient)
}