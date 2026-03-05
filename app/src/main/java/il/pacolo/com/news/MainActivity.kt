package il.pacolo.com.news

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import dagger.hilt.android.AndroidEntryPoint
import il.pacolo.com.news.presentation.screens.WeatherScreen
import il.pacolo.com.news.presentation.viewmodels.TaskViewModel
import il.pacolo.com.news.security.NativeKeys
import il.pacolo.com.news.ui.theme.NewsTheme
import kotlin.math.log

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NewsTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    WeatherScreen(

                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier, onClick: () -> Unit) {

    Text(text = NativeKeys.getWeatherApiKey())
}


@Composable
fun Greeting_(name: String, modifier: Modifier = Modifier, onClick: () -> Unit) {

    val viewModel: TaskViewModel = viewModel()
    val nombre = rememberSaveable { mutableStateOf("") }

    viewModel.fetchUser(1) { result ->


        result.fold(
            onSuccess = { user ->

                nombre.value = user.name

            },
            onFailure = { exception ->

                nombre.value = "Error " + exception.message.toString()

            }


        )

    }

    Scaffold() {
        Column(
            modifier = Modifier.padding(it)
        ) {
            Text(
                text = "Hello ${nombre.value}!",
                modifier = modifier
            )

            Button(
                onClick = {
                    onClick()
                    viewModel.getOrders()
                }
            ) {
                Text("Get Orders")
            }
        }
    }

}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    NewsTheme {
        Greeting("Android",modifier = Modifier.fillMaxSize(), onClick = {

        })
    }
}