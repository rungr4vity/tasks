package il.pacolo.com.news.presentation.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import il.pacolo.com.news.presentation.viewmodels.WeatherViewModel
import android.Manifest
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.text.style.TextAlign
import com.google.accompanist.permissions.*
import il.pacolo.com.news.data.remote.dto.WeatherResponse


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun WeatherScreen(
    modifier: Modifier = Modifier,
    viewModel: WeatherViewModel = hiltViewModel()
) {
    val weather by viewModel.weather.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    val locationPermission = rememberPermissionState(
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    // On launch: request location OR load last city
    LaunchedEffect(Unit) {
        if (locationPermission.status.isGranted) {
            viewModel.fetchByCurrentLocation()
        } else {
            locationPermission.launchPermissionRequest()
        }
    }

    // When permission result comes back
    LaunchedEffect(locationPermission.status) {
        when {
            locationPermission.status.isGranted -> viewModel.fetchByCurrentLocation()
            locationPermission.status.shouldShowRationale -> viewModel.loadLastCity()
            else -> viewModel.loadLastCity()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F2027),
                        Color(0xFF203A43),
                        Color(0xFF2C5364)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            Text("Weather", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("Search any city", fontSize = 14.sp, color = Color.White.copy(alpha = 0.5f))

            Spacer(modifier = Modifier.height(32.dp))

            // Location permission banner
            AnimatedVisibility(
                visible = !locationPermission.status.isGranted &&
                        locationPermission.status.shouldShowRationale
            ) {
                LocationBanner(onAllow = { locationPermission.launchPermissionRequest() })
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = {
                    Text("e.g. New York, London...", color = Color.White.copy(alpha = 0.4f))
                },
                leadingIcon = {
                    Icon(Icons.Rounded.Search, contentDescription = null, tint = Color.White.copy(alpha = 0.7f))
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Rounded.Clear, contentDescription = "Clear", tint = Color.White.copy(alpha = 0.7f))
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        if (query.isNotBlank()) {
                            viewModel.fetchByCity(query.trim())
                            focusManager.clearFocus()
                        }
                    }
                ),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF4FC3F7),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color(0xFF4FC3F7),
                    focusedContainerColor = Color.White.copy(alpha = 0.08f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.05f)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Search button
                Button(
                    onClick = {
                        if (query.isNotBlank()) {
                            viewModel.fetchByCity(query.trim())
                            focusManager.clearFocus()
                        }
                    },
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4FC3F7))
                ) {
                    Icon(Icons.Rounded.Search, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Search", fontWeight = FontWeight.SemiBold)
                }

                // Location button
                if (locationPermission.status.isGranted) {
                    IconButton(
                        onClick = { viewModel.fetchByCurrentLocation() },
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.1f))
                    ) {
                        Icon(Icons.Rounded.LocationOn, contentDescription = "Use location", tint = Color(0xFF4FC3F7))
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Loading
            AnimatedVisibility(visible = isLoading) {
                CircularProgressIndicator(color = Color(0xFF4FC3F7))
            }

            // Error
            AnimatedVisibility(visible = error != null && !isLoading, enter = fadeIn() + slideInVertically(), exit = fadeOut()) {
                ErrorCard(message = error ?: "")
            }

            // Weather result
            AnimatedVisibility(visible = weather != null && !isLoading && error == null, enter = fadeIn() + slideInVertically(), exit = fadeOut()) {
                weather?.let { WeatherCard(it) }
            }

            // Empty state
            AnimatedVisibility(visible = weather == null && !isLoading && error == null, enter = fadeIn(), exit = fadeOut()) {
                EmptyState()
            }
        }
    }
}
@Composable
private fun WeatherCard(weather: WeatherResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            weather.name?.let {
                Text(
                    text = it,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            weather.sys?.country?.let {
                Text(
                    text = it,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "${weather.main?.temp?.toInt()}°F",
                fontSize = 72.sp,
                fontWeight = FontWeight.Thin,
                color = Color.White
            )
            Text(
                text = weather.weather?.firstOrNull()?.description
                    ?.replaceFirstChar { it.uppercase() } ?: "",
                fontSize = 16.sp,
                color = Color(0xFF4FC3F7)
            )

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                WeatherStat(
                    icon = Icons.Rounded.Face,
                    label = "Feels like",
                    value = "${weather.main?.feelsLike?.toInt()}°F"
                )
                WeatherStat(
                    icon = Icons.Rounded.Face,
                    label = "Humidity",
                    value = "${weather.main?.humidity}%"
                )
                WeatherStat(
                    icon = Icons.Rounded.Face,
                    label = "Wind",
                    value = "${weather.wind?.speed} mph"
                )
            }
        }
    }
}

@Composable
private fun WeatherStat(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = Color(0xFF4FC3F7),
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(value, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Text(label, color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
    }
}

@Composable
private fun EmptyState() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(top = 48.dp)
    ) {
        Text("🌤️", fontSize = 64.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Search for a city\nto see its weather",
            color = Color.White.copy(alpha = 0.4f),
            textAlign = TextAlign.Center,
            fontSize = 16.sp,
            lineHeight = 24.sp
        )
    }
}
@Composable
private fun ErrorCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFB71C1C).copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Rounded.Warning,
                contentDescription = null,
                tint = Color(0xFFEF9A9A)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = message,
                color = Color(0xFFEF9A9A),
                fontSize = 14.sp
            )
        }
    }
}
@Composable
private fun LocationBanner(onAllow: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF4FC3F7).copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.LocationOn, contentDescription = null, tint = Color(0xFF4FC3F7))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                "Allow location for local weather",
                color = Color.White,
                fontSize = 13.sp,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onAllow) {
                Text("Allow", color = Color(0xFF4FC3F7), fontWeight = FontWeight.Bold)
            }
        }
    }
}