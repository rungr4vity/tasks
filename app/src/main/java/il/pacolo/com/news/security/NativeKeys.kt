package il.pacolo.com.news.security

object NativeKeys {
    init {
        System.loadLibrary("keys")
    }

    external fun getWeatherApiKey(): String
}