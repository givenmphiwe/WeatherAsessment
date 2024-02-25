import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

object WeatherFetcher {
    private const val API: String = "f01e80368f05c66b03425d3f08ab1a1c"

    fun fetchWeather(context: Context, city: String, callback: (String) -> Unit) {
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val response = withContext(Dispatchers.IO) {
                    URL("https://api.openweathermap.org/data/2.5/weather?q=$city&units=metric&appid=$API")
                        .readText(Charsets.UTF_8)
                }
                callback.invoke(response)
            } catch (e: Exception) {
                callback.invoke("Failed to fetch weather data: ${e.message}")
            }
        }
    }
}
