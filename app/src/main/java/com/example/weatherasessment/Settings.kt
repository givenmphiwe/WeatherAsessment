package com.example.weatherasessment

import Home
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Settings.newInstance] factory method to
 * create an instance of this fragment.
 */
class Settings : Fragment() {
    // TODO: Rename and change types of parameters

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_settings, container, false)

        val editTextCity = rootView.findViewById<EditText>(R.id.editTextCity)
        val buttonSubmit = rootView.findViewById<Button>(R.id.buttonSubmit)
        sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        val lastEnteredCity = sharedPreferences.getString("CITY", "Johannesburg")
        editTextCity.setText(lastEnteredCity)

        buttonSubmit.setOnClickListener {
            val city = editTextCity.text.toString().trim()
            sharedPreferences.edit().putString("CITY", city).apply()

            val homeFragment = Home().apply {
                arguments = Bundle().apply {
                    putString("CITY", city)
                }
            }

            requireActivity().supportFragmentManager.beginTransaction()

                .addToBackStack(null)  // This allows the user to navigate back to the previous fragment
                .commit()

            // Show a toast message
            Toast.makeText(requireContext(), "City updated to $city", Toast.LENGTH_SHORT).show()
        }



        return rootView
    }
}