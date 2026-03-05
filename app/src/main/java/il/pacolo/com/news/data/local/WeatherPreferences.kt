package il.pacolo.com.news.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "weather_prefs")

@Singleton
class WeatherPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val LAST_CITY_KEY = stringPreferencesKey("last_city")
    }

    val lastCity: Flow<String?> = context.dataStore.data
        .map { it[LAST_CITY_KEY] }

    suspend fun saveLastCity(city: String) {
        context.dataStore.edit { it[LAST_CITY_KEY] = city }
    }
}