import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.weatherasessment.R
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class Home : Fragment() {

    private val API: String = "f01e80368f05c66b03425d3f08ab1a1c"

    lateinit var sharedPreferences: SharedPreferences
    var CITY: String = "Johannesburg" // Default value

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            LocationHelper.getCurrentLocation(requireActivity()) { location ->
                // Convert latitude and longitude to city name
                val geocoder = Geocoder(requireContext(), Locale.getDefault())
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)

                // Update the CITY variable with the city name
                CITY = addresses?.get(0)?.locality ?: "Johannesburg"

                // Save the updated CITY to SharedPreferences
                with(sharedPreferences.edit()) {
                    putString("CITY", CITY)
                    apply()
                }
            }
        } else {
            // Location permission is not granted, use default city
            CITY = sharedPreferences.getString("CITY", "Johannesburg") ?: "Johannesburg"
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_home, container, false)

        if (NetworkHelper.isNetworkAvailable(requireContext())) {
            WeatherFetcher.fetchWeather(requireContext(), CITY) { response ->
                if (response.isNotEmpty()) {
                    handleWeatherResponse(rootView, response)
                } else {
                    showError(rootView, "Failed to fetch weather data")
                }
            }
        } else {
            showError(rootView, "Network not available")
        }

        setupPopupListeners(rootView)

        return rootView
    }


    fun handleWeatherResponse(rootView: View, response: String) {
        try {
            val jsonObj = JSONObject(response)

            if (jsonObj.has("cod") && jsonObj.getInt("cod") != 200) {
                // If response contains error code, handle error
                val errorMessage = jsonObj.getString("message")
                showError(rootView, errorMessage)
                return
            }

            val main = jsonObj.getJSONObject("main")
            val sys = jsonObj.getJSONObject("sys")
            val wind = jsonObj.getJSONObject("wind")
            val weatherArray = jsonObj.getJSONArray("weather")

            if (weatherArray.length() == 0) {
                // If weather information not found, handle error
                showError(rootView, "Weather information not available")
                return
            }

            val weather = weatherArray.getJSONObject(0)

            val updatedAt: Long = jsonObj.getLong("dt")
            val updatedAtText = "Updated at: " + SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.ENGLISH)
                .format(Date(updatedAt * 1000))

            val temp = main.optString("temp", "") + "°C"
            val tempMin = "Min Temp: " + main.optString("temp_min", "") + "°C"
            val tempMax = "Max Temp: " + main.optString("temp_max", "") + "°C"
            val pressure = main.optString("pressure", "")
            val humidity = main.optString("humidity", "")

            val sunrise: Long = sys.optLong("sunrise", 0)
            val sunset: Long = sys.optLong("sunset", 0)
            val windSpeed = wind.optString("speed", "")
            val weatherDescription = weather.optString("description", "")

            val address = jsonObj.optString("name", "") + ", " + sys.optString("country", "")

            // Populating extracted data into our views
            rootView.findViewById<TextView>(R.id.address).text = address
            rootView.findViewById<TextView>(R.id.updated_at).text = updatedAtText
            rootView.findViewById<TextView>(R.id.status).text = weatherDescription.capitalize()
            rootView.findViewById<TextView>(R.id.temp).text = temp
            rootView.findViewById<TextView>(R.id.temp_min).text = tempMin
            rootView.findViewById<TextView>(R.id.temp_max).text = tempMax
            rootView.findViewById<TextView>(R.id.sunrise).text =
                SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunrise * 1000))
            rootView.findViewById<TextView>(R.id.sunset).text =
                SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunset * 1000))
            rootView.findViewById<TextView>(R.id.wind).text = windSpeed
            rootView.findViewById<TextView>(R.id.pressure).text = pressure
            rootView.findViewById<TextView>(R.id.humidity).text = humidity

            // Views populated, Hiding the loader, Showing the main design
            rootView.findViewById<ProgressBar>(R.id.loader).visibility = View.GONE
            rootView.findViewById<RelativeLayout>(R.id.mainContainer).visibility = View.VISIBLE

        } catch (e: Exception) {
            showError(rootView, "Please enter a valid City")
            e.printStackTrace()
        }
    }


    fun showError(rootView: View, message: String) {
        rootView.findViewById<ProgressBar>(R.id.loader).visibility = View.GONE
        rootView.findViewById<TextView>(R.id.errorText).visibility = View.VISIBLE
        rootView.findViewById<TextView>(R.id.errorText).text = message
    }

    fun showPopup(anchorView: View, title: String, detail: String) {
        val popupView = layoutInflater.inflate(R.layout.popup_layout, null)

        val popupTitle = popupView.findViewById<TextView>(R.id.popup_title)
        val popupDetail = popupView.findViewById<TextView>(R.id.popup_detail)

        popupTitle.text = title
        popupDetail.text = detail

        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        popupWindow.showAsDropDown(anchorView, 0, 0)
    }

    private fun setupPopupListeners(rootView: View) {
        rootView.findViewById<TextView>(R.id.sunrise).setOnClickListener { view ->
            val textView = view as TextView
            showPopup(textView, "Sunrise", "Time of sunrise: " + textView.text)
        }

        rootView.findViewById<TextView>(R.id.sunset).setOnClickListener { view ->
            val textView = view as TextView
            showPopup(textView, "Sunset", "Time of sunset: " + textView.text)
        }

        rootView.findViewById<TextView>(R.id.wind).setOnClickListener { view ->
            val textView = view as TextView
            showPopup(textView, "Wind", "The wind today is blowing at: " + textView.text + " km/h")
        }

        rootView.findViewById<TextView>(R.id.pressure).setOnClickListener { view ->
            val textView = view as TextView
            showPopup(textView, "Pressure", "Atmosphere pressure is measured at: " + textView.text)
        }

        rootView.findViewById<TextView>(R.id.humidity).setOnClickListener { view ->
            val textView = view as TextView
            showPopup(textView, "Humidity", "The humidity today is: " + textView.text + " %")
        }

    }
}
