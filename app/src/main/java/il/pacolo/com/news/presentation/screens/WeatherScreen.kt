package il.pacolo.com.news.presentation.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import il.pacolo.com.news.data.remote.dto.WeatherResponse

import il.pacolo.com.news.presentation.viewmodels.WeatherViewModel

@Composable
fun WeatherScreen(
    viewModel: WeatherViewModel = hiltViewModel()
) {
    val weather by viewModel.weather.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier
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

            // Title
            Text(
                text = "Weather",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = "Search any city",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Search Bar
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = {
                    Text(
                        "e.g. New York, London...",
                        color = Color.White.copy(alpha = 0.4f)
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Rounded.Search,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.7f)
                    )
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(
                                Icons.Rounded.Clear,
                                contentDescription = "Clear",
                                tint = Color.White.copy(alpha = 0.7f)
                            )
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

            // Search Button
            Button(
                onClick = {
                    if (query.isNotBlank()) {
                        viewModel.fetchByCity(query.trim())
                        focusManager.clearFocus()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4FC3F7)
                )
            ) {
                Icon(Icons.Rounded.Search, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Search", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Content Area
            AnimatedVisibility(
                visible = error != null,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut()
            ) {
                ErrorCard(message = error ?: "")
            }

            AnimatedVisibility(
                visible = weather != null && error == null,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut()
            ) {
                weather?.let { WeatherCard(it) }
            }

            // Empty state
            AnimatedVisibility(
                visible = weather == null && error == null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
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
            // City name
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

            // Temperature
            Text(
                text = "${weather.main?.temp?.toInt()}°F",
                fontSize = 72.sp,
                fontWeight = FontWeight.Thin,
                color = Color.White
            )

            Text(
                text = weather.weather?.firstOrNull()?.description?.replaceFirstChar { it.uppercase() } ?: "",
                fontSize = 16.sp,
                color = Color(0xFF4FC3F7)
            )

            Spacer(modifier = Modifier.height(24.dp))

            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

            Spacer(modifier = Modifier.height(24.dp))

            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                WeatherStat(
                    icon = Icons.Rounded.KeyboardArrowDown,
                    label = "Feels like",
                    value = "${weather.main?.feelsLike?.toInt()}°F"
                )
                WeatherStat(
                    icon = Icons.Rounded.KeyboardArrowDown,
                    label = "Humidity",
                    value = "${weather.main?.humidity}%"
                )
                WeatherStat(
                    icon = Icons.Rounded.KeyboardArrowDown,
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
            Icon(icon, contentDescription = null, tint = Color(0xFF4FC3F7), modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(value, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Text(label, color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
    }
}

@Composable
private fun ErrorCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFB71C1C).copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.Warning, contentDescription = null, tint = Color(0xFFEF9A9A))
            Spacer(modifier = Modifier.width(12.dp))
            Text(message, color = Color(0xFFEF9A9A), fontSize = 14.sp)
        }
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
            "Search for a city\nto see its weather",
            color = Color.White.copy(alpha = 0.4f),
            textAlign = TextAlign.Center,
            fontSize = 16.sp,
            lineHeight = 24.sp
        )
    }
}