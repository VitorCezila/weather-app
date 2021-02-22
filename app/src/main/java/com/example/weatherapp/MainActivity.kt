package com.example.weatherapp

import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import java.lang.Exception
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    var CITY: String = "new york"
    val API: String = "7c721ab3de8e74a63f11cebed3ec5739"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val searchView = findViewById<SearchView>(R.id.searchView)

        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                //change CITY
                searchForWeather(query.toString())
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }

        })

        searchForWeather(CITY)

        //when user refresh
        swipeToRefresh.setOnRefreshListener {
            if(tv_errorText.visibility == View.VISIBLE) {
                searchForWeather("São Paulo")
                Toast.makeText(applicationContext, "São Paulo was set as the default in cases of error", Toast.LENGTH_SHORT).show()
                swipeToRefresh.isRefreshing = false
                searchView.setQuery("", false)
                searchView.clearFocus()
            } else {
                searchForWeather(CITY)
                swipeToRefresh.isRefreshing = false
            }
        }
    }

    private fun searchForWeather(location: String) {
        weatherTask().execute()
        CITY = location
    }


    inner class weatherTask() : AsyncTask<String, Void, String>() {
        override fun onPreExecute() {
            super.onPreExecute()
            findViewById<ProgressBar>(R.id.pb_loader).visibility = View.VISIBLE
            findViewById<ConstraintLayout>(R.id.mainContainer).visibility = View.GONE
            findViewById<TextView>(R.id.tv_errorText).visibility = View.GONE
        }

        //fetch the api data and extract it
        override fun doInBackground(vararg params: String?): String? {
            var response: String?
            try {
                response =
                    URL("https://api.openweathermap.org/data/2.5/weather?q=$CITY&units=metric&appid=$API").readText(
                        Charsets.UTF_8
                    )
            } catch (e: Exception) {
                response = null
            }
            return response
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            try {

                val jsonObj = JSONObject(result)
                val main = jsonObj.getJSONObject("main")
                val sys = jsonObj.getJSONObject("sys")
                val wind = jsonObj.getJSONObject("wind")
                val weather = jsonObj.getJSONArray("weather").getJSONObject(0)
                val updatedAt: Long = jsonObj.getLong("dt")

                val updatedAtText = "Updated at: " + SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.ENGLISH).format(Date(updatedAt * 1000))
                val temp = main.getString("temp") + "°C"
                val tempMin = "Min: " + main.getString("temp_min") + "°C"
                val tempMax = "Max: " + main.getString("temp_max") + "°C"
                val pressure = main.getString("pressure") + "hPa"
                val humidity = main.getString("humidity") + "%"
                val sunrise: Long = sys.getLong("sunrise")
                val sunset: Long = sys.getLong("sunset")
                val windSpeed = wind.getString("speed") + "m/s"
                val weatherDescription = weather.getString("description")
                val address = jsonObj.getString("name") + ", " + sys.getString("country")

                val weatherDescriptionIcon = weather.getString("icon")

                //populate textViews with data
                findViewById<TextView>(R.id.tv_location).text = address
                findViewById<TextView>(R.id.tv_updateAt).text = updatedAtText
                findViewById<TextView>(R.id.tv_status).text = weatherDescription.capitalize()
                findViewById<TextView>(R.id.tv_temp).text = temp
                findViewById<TextView>(R.id.tv_min_temp).text = tempMin
                findViewById<TextView>(R.id.tv_max_temp).text = tempMax
                findViewById<TextView>(R.id.tv_sunrise).text = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunrise * 1000))
                findViewById<TextView>(R.id.tv_sunset).text = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunset * 1000))
                findViewById<TextView>(R.id.tv_wind).text = windSpeed
                findViewById<TextView>(R.id.tv_pressure).text = pressure
                findViewById<TextView>(R.id.tv_humidity).text = humidity

                //disabling progressbar and activating mainContainer
                findViewById<ProgressBar>(R.id.pb_loader).visibility = View.GONE
                findViewById<ConstraintLayout>(R.id.mainContainer).visibility = View.VISIBLE
            }
            //error message
            catch (e: Exception) {
                findViewById<ProgressBar>(R.id.pb_loader).visibility = View.GONE
                findViewById<TextView>(R.id.tv_errorText).visibility = View.VISIBLE
                Toast.makeText(applicationContext, "There was an error fetching weather, " + "try again.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
