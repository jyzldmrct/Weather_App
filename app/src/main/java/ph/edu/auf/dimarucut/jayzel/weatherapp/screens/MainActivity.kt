package ph.edu.auf.dimarucut.jayzel.weatherapp.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import ph.edu.auf.dimarucut.jayzel.weatherapp.ui.theme.WeatherAppTheme

class MainActivity : ComponentActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        setContent {
            WeatherAppTheme {
                WeatherScreen(fusedLocationClient)
            }
        }
    }
}
