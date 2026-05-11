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
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.meteo.app.data.WeatherRepository
import com.meteo.app.data.local.LocationStore
import com.meteo.app.domain.SavedLocation
import com.meteo.app.ui.WeatherRoute
import com.meteo.app.ui.WeatherViewModel
import com.meteo.app.ui.WeatherViewModelFactory
import com.meteo.app.ui.theme.MeteoTheme

class MainActivity : ComponentActivity() {

    private val fused by lazy { LocationServices.getFusedLocationProviderClient(this) }
    private val locationStore by lazy { LocationStore(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MeteoTheme {
                val vm: WeatherViewModel = viewModel(
                    factory = WeatherViewModelFactory(WeatherRepository(), locationStore),
                )

                val permissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestMultiplePermissions(),
                ) { results ->
                    val granted = (results[Manifest.permission.ACCESS_FINE_LOCATION] == true) ||
                        (results[Manifest.permission.ACCESS_COARSE_LOCATION] == true)
                    if (granted) {
                        requestDeviceLocation { lat, lon ->
                            vm.load(SavedLocation(getString(R.string.my_location), lat, lon))
                        }
                    } else {
                        // Fallback to default if permission denied
                        vm.load(SavedLocation("Paris", 48.856614, 2.3522219))
                    }
                }

                LaunchedEffect(Unit) {
                    if (hasLocationPermission()) {
                        requestDeviceLocation { lat, lon ->
                            vm.load(SavedLocation(getString(R.string.my_location), lat, lon))
                        }
                    } else {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                            ),
                        )
                    }
                }

                WeatherRoute(
                    viewModel = vm,
                    onRequestLocation = {
                        if (hasLocationPermission()) {
                            requestDeviceLocation { lat, lon ->
                                vm.load(SavedLocation(getString(R.string.my_location), lat, lon))
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
                    onRefresh = {
                        val state = vm.state.value
                        if (state is com.meteo.app.ui.WeatherUiState.Success) {
                            vm.load(state.currentLocation)
                        }
                    }
                )
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
    }
}
