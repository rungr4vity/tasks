package il.pacolo.com.news.presentation.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import il.pacolo.com.news.presentation.viewmodels.WeatherViewModel

// ─── Data Models ────────────────────────────────────────────────────────────

enum class WeatherCondition {
    CLEAR, CLOUDY, RAINY, STORMY, SNOWY, FOGGY
}

data class WeatherDetail(val label: String, val value: String, val icon: String)

data class WeatherUiState(
    val cityName: String = "",
    val country: String = "",
    val temperature: Int = 0,
    val feelsLike: Int = 0,
    val condition: WeatherCondition = WeatherCondition.CLEAR,
    val conditionLabel: String = "",
    val humidity: Int = 0,
    val windSpeed: Int = 0,
    val visibility: Int = 0,
    val uvIndex: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)

// ─── Color Schemes per Weather Condition ─────────────────────────────────────

private val weatherPalette: Map<WeatherCondition, Pair<Color, Color>> = mapOf(
    WeatherCondition.CLEAR   to (Color(0xFF0F2B5B) to Color(0xFF1B6CA8)),
    WeatherCondition.CLOUDY  to (Color(0xFF2C3E50) to Color(0xFF4A5568)),
    WeatherCondition.RAINY   to (Color(0xFF1A2A3A) to Color(0xFF2D4A6B)),
    WeatherCondition.STORMY  to (Color(0xFF1A1A2E) to Color(0xFF16213E)),
    WeatherCondition.SNOWY   to (Color(0xFF2C3E7A) to Color(0xFF8BB8D0)),
    WeatherCondition.FOGGY   to (Color(0xFF3A4A52) to Color(0xFF7A8E95))
)

private val conditionEmoji: Map<WeatherCondition, String> = mapOf(
    WeatherCondition.CLEAR   to "☀️",
    WeatherCondition.CLOUDY  to "⛅",
    WeatherCondition.RAINY   to "🌧️",
    WeatherCondition.STORMY  to "⛈️",
    WeatherCondition.SNOWY   to "❄️",
    WeatherCondition.FOGGY   to "🌫️"
)

// ─── Main Screen ─────────────────────────────────────────────────────────────


@Composable
fun WeatherScreen_(
    viewModel: WeatherViewModel = hiltViewModel()
) {
    val weather by viewModel.weather.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        //viewModel.fetchWeatherByCity("New York")
    }

    when {
        error != null -> Text("Error: $error")
        weather != null -> Text("Temp: ${weather!!.main?.temp}°F")
        else -> CircularProgressIndicator()
    }
}


@Composable
fun WeatherScreen(
    uiState: WeatherUiState,
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    val (gradientTop, gradientBottom) = weatherPalette[uiState.condition]
        ?: (Color(0xFF0F2B5B) to Color(0xFF1B6CA8))

    val animatedTop by animateColorAsState(
        targetValue = gradientTop,
        animationSpec = tween(durationMillis = 800, easing = EaseInOutCubic),
        label = "gradientTop"
    )
    val animatedBottom by animateColorAsState(
        targetValue = gradientBottom,
        animationSpec = tween(durationMillis = 800, easing = EaseInOutCubic),
        label = "gradientBottom"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(animatedTop, animatedBottom)
                )
            )
    ) {
        // Decorative blurred orb
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = (-60).dp, y = (-60).dp)
                .blur(80.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.12f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 56.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ── Search Bar ──
            WeatherSearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                onSearch = {
                    onSearch(searchQuery)
                    focusManager.clearFocus()
                },
                onClear = { searchQuery = "" }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ── Weather Content ──
            AnimatedContent(
                targetState = uiState.isLoading,
                transitionSpec = {
                    fadeIn(tween(400)) togetherWith fadeOut(tween(200))
                },
                label = "weatherContent"
            ) { loading ->
                if (loading) {
                    WeatherLoadingIndicator()
                } else if (uiState.error != null) {
                    WeatherErrorCard(message = uiState.error)
                } else if (uiState.cityName.isNotEmpty()) {
                    WeatherContent(uiState = uiState)
                } else {
                    WeatherEmptyState()
                }
            }
        }
    }
}

// ─── Search Bar ──────────────────────────────────────────────────────────────

@Composable
private fun WeatherSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    val glassColor = Color.White.copy(alpha = 0.15f)
    val borderColor = Color.White.copy(alpha = 0.25f)

    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            ),
        placeholder = {
            Text(
                text = "Search city…",
                color = Color.White.copy(alpha = 0.55f),
                fontSize = 15.sp
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color.White.copy(alpha = 0.8f)
            )
        },
        trailingIcon = {
            AnimatedVisibility(
                visible = query.isNotEmpty(),
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                IconButton(onClick = onClear) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear",
                        tint = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = glassColor,
            unfocusedContainerColor = glassColor,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = Color.White,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        )
    )
}

// ─── Weather Content ──────────────────────────────────────────────────────────

@Composable
private fun WeatherContent(uiState: WeatherUiState) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // City & Country
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = uiState.cityName,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 0.5.sp
            )
            if (uiState.country.isNotEmpty()) {
                Text(
                    text = uiState.country,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.65f),
                    letterSpacing = 2.sp
                )
            }
        }

        // Weather Icon + Temperature
        WeatherHero(
            emoji = conditionEmoji[uiState.condition] ?: "🌤️",
            temperature = uiState.temperature,
            feelsLike = uiState.feelsLike,
            conditionLabel = uiState.conditionLabel
        )

        // Stats Grid
        WeatherStatsGrid(uiState = uiState)
    }
}

@Composable
private fun WeatherHero(
    emoji: String,
    temperature: Int,
    feelsLike: Int,
    conditionLabel: String
) {
    val infiniteTransition = rememberInfiniteTransition(label = "hero")
    val floatAnim by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = emoji,
            fontSize = 96.sp,
            modifier = Modifier.offset(y = floatAnim.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "${temperature}°",
            fontSize = 80.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            lineHeight = 80.sp
        )
        Text(
            text = conditionLabel,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.75f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Feels like ${feelsLike}°",
            fontSize = 13.sp,
            color = Color.White.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun WeatherStatsGrid(uiState: WeatherUiState) {
    val details = listOf(
        WeatherDetail("Humidity",    "${uiState.humidity}%",       "💧"),
        WeatherDetail("Wind",        "${uiState.windSpeed} km/h",  "💨"),
        WeatherDetail("Visibility",  "${uiState.visibility} km",   "👁️"),
        WeatherDetail("UV Index",    "${uiState.uvIndex}",         "🔆")
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        details.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { detail ->
                    WeatherDetailCard(
                        detail = detail,
                        modifier = Modifier.weight(1f)
                    )
                }
                // If odd number of items, fill last slot
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun WeatherDetailCard(detail: WeatherDetail, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.12f))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.2f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(vertical = 18.dp, horizontal = 16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = detail.icon,
                fontSize = 22.sp
            )
            Text(
                text = detail.value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = detail.label,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.55f),
                letterSpacing = 0.8.sp
            )
        }
    }
}

// ─── States: Loading / Error / Empty ─────────────────────────────────────────

@Composable
private fun WeatherLoadingIndicator() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(top = 60.dp)
    ) {
        CircularProgressIndicator(
            color = Color.White,
            strokeWidth = 2.dp,
            modifier = Modifier.size(40.dp)
        )
        Text(
            text = "Fetching weather…",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 15.sp
        )
    }
}

@Composable
private fun WeatherErrorCard(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFB02020).copy(alpha = 0.35f))
            .border(
                width = 1.dp,
                color = Color(0xFFFF6B6B).copy(alpha = 0.4f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "⚠️", fontSize = 32.sp)
            Text(
                text = message,
                color = Color.White,
                fontSize = 15.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun WeatherEmptyState() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(top = 60.dp)
    ) {
        Text(text = "🌍", fontSize = 64.sp)
        Text(
            text = "Search for a city\nto see the weather",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}

// ─── Preview ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun WeatherScreenPreview() {
    WeatherScreen(
        uiState = WeatherUiState(
            cityName = "Monterrey",
            country = "MX",
            temperature = 28,
            feelsLike = 31,
            condition = WeatherCondition.CLEAR,
            conditionLabel = "Sunny",
            humidity = 52,
            windSpeed = 18,
            visibility = 10,
            uvIndex = 7
        ),
        onSearch = {}
    )
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun WeatherScreenRainyPreview() {
    WeatherScreen(
        uiState = WeatherUiState(
            cityName = "London",
            country = "UK",
            temperature = 12,
            feelsLike = 9,
            condition = WeatherCondition.RAINY,
            conditionLabel = "Light Rain",
            humidity = 88,
            windSpeed = 24,
            visibility = 5,
            uvIndex = 1
        ),
        onSearch = {}
    )
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun WeatherScreenEmptyPreview() {
    WeatherScreen(
        uiState = WeatherUiState(),
        onSearch = {}
    )
}