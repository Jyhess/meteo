package com.meteo.app

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.meteo.app.data.WeatherRepository
import com.meteo.app.ui.WeatherRoute
import com.meteo.app.ui.WeatherViewModel
import com.meteo.app.ui.WeatherViewModelFactory
import com.meteo.app.ui.theme.MeteoTheme

class MainActivity : ComponentActivity() {

    private val fused by lazy { LocationServices.getFusedLocationProviderClient(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MeteoTheme {
                val vm: WeatherViewModel = viewModel(
                    factory = WeatherViewModelFactory(WeatherRepository()),
                )
                var coords by remember {
                    mutableStateOf(
                        Triple(
                            DEFAULT_LAT,
                            DEFAULT_LON,
                            getString(R.string.location_paris),
                        ),
                    )
                }

                val permissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestMultiplePermissions(),
                ) { results ->
                    val granted = (results[Manifest.permission.ACCESS_FINE_LOCATION] == true) ||
                        (results[Manifest.permission.ACCESS_COARSE_LOCATION] == true)
                    if (granted) {
                        requestDeviceLocation { lat, lon ->
                            coords = Triple(lat, lon, getString(R.string.my_location))
                        }
                    }
                }

                LaunchedEffect(coords) {
                    vm.load(coords.first, coords.second, coords.third)
                }

                WeatherRoute(
                    viewModel = vm,
                    onRequestLocation = {
                        if (hasLocationPermission()) {
                            requestDeviceLocation { lat, lon ->
                                coords = Triple(lat, lon, getString(R.string.my_location))
                            }
                        } else {
                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION,
                                ),
                            )
                        }
                    },
                ) {
                    vm.load(coords.first, coords.second, coords.third)
                }
            }
        }
    }

    private fun hasLocationPermission(): Boolean =
        (ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED) ||
            (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED)

    @SuppressLint("MissingPermission")
    private fun requestDeviceLocation(onResult: (Double, Double) -> Unit) {
        if (!hasLocationPermission()) return
        fused.lastLocation.addOnSuccessListener { loc ->
            if (loc != null) {
                onResult(loc.latitude, loc.longitude)
            } else {
                fused.getCurrentLocation(
                    Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                    CancellationTokenSource().token,
                ).addOnSuccessListener { l ->
                    if (l != null) {
                        onResult(l.latitude, l.longitude)
                    }
                }
            }
        }
    }

    companion object {
        private const val DEFAULT_LAT = 48.856614
        private const val DEFAULT_LON = 2.3522219
    }
}
