package com.example.capstonedesignproject

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.Manifest
import android.content.ActivityNotFoundException
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.ViewGroup
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.core.app.ActivityCompat
import com.example.capstonedesignproject.chatbot.ApiService
import com.example.capstonedesignproject.chatbot.Message
import com.example.capstonedesignproject.chatbot.OpenAIRequestBody
import com.example.capstonedesignproject.databinding.ActivityMainBinding
import com.example.capstonedesignproject.history.HistoryActivity
import com.example.capstonedesignproject.integration.NearbyHospitalActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL


class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    private lateinit var database : DatabaseReference

    private lateinit var roomTemperatureTextView: TextView
    private lateinit var roomHumidityTextView: TextView
    private lateinit var bodyTemperatureTextView: TextView
    private lateinit var airQualityTextView: TextView
    private lateinit var bpmTextView: TextView

    private val handler = Handler()

    // Add a variable to keep track of whether the analysis should be performed
    private var shouldAnalyze: Boolean = false
    private var responseAnalysis : String = ""


    // Function to handle the "Analyze" button click
    fun onAnalyzeClicked() {
        shouldAnalyze = true
    }


    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.menu_home -> {
                // Navigate to MainActivity
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                return@OnNavigationItemSelectedListener true
            }
            R.id.menu_history -> {
                // Navigate to HistoryActivity
                val intent = Intent(this, HistoryActivity::class.java)
                startActivity(intent)
                return@OnNavigationItemSelectedListener true
            }
            R.id.menu_hospital -> {
                // Navigate to NearbyHospitalActivity
                val intent = Intent(this, NearbyHospitalActivity::class.java)
                startActivity(intent)
                return@OnNavigationItemSelectedListener true
            }
            R.id.menu_chatbot -> {
                // Navigate to ChatbotActivity
                val intent = Intent(this, ChatbotActivity::class.java)
                startActivity(intent)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }


    // Function to send an email using the default email client
    fun sendEmail(emailAddress: String, subject: String, body: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$emailAddress")
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }

        startActivity(intent)
    }

    // Function to send a WhatsApp message using the WhatsApp application
    fun sendMessage(phoneNumber: String, message: String, context: AppCompatActivity) {

        val whatsappUrl = Uri.parse("whatsapp://send?phone=$phoneNumber${if (message.isNotEmpty()) "&text=" + message.replace(" ", "%20") else ""}")

        val intent = Intent(Intent.ACTION_VIEW, whatsappUrl)

        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            context.toast("Gagal membuka WhatsApp. Pastikan WhatsApp terpasang dan konfigurasinya benar.")
        }
    }

    // Function to initiate a phone call to the specified phone number
    fun callPhoneNumber(phoneNumber: String, context: AppCompatActivity) {
        // Validate phone number
        if (phoneNumber.isEmpty() || !phoneNumber.matches(Regex("[0-9]+"))) {
            context.toast("Nomor telepon tidak valid: $phoneNumber")
            return
        }

        // Check CALL_PHONE permissions
        if (context.checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            // Request CALL_PHONE permissions
            ActivityCompat.requestPermissions(context, arrayOf(Manifest.permission.CALL_PHONE), REQUEST_CALL_PHONE)
            return
        }

        // Create URI for phone call
        val callUri = Uri.parse("tel:$phoneNumber")

        // Create Intent with ACTION_CALL
        val intent = Intent(Intent.ACTION_CALL, callUri)

        // Make the call
        context.startActivity(intent)
    }

    private val REQUEST_CALL_PHONE = 100

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CALL_PHONE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Izin CALL_PHONE diizinkan, lakukan panggilan
                callPhoneNumber("085710056837", this)
            }
        }
    }

    // Function to display a Toast message
    fun AppCompatActivity.toast(message: String) {
        val toast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
        toast.show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        overridePendingTransition(0, 0);

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Save data button click listener
        binding.btnSaveData.setOnClickListener {

            val name = binding.etName.text.toString()
            val age = binding.etAge.text.toString()
            val bpm = binding.bpmTextView.text.toString()
            val bodyTemp = binding.bodyTemperatureTextView.text.toString()
            val airQuality = binding.airQualityTextView.text.toString()
            val roomTemp = binding.roomTemperatureTextView.text.toString()
            val roomHumid = binding.roomHumidityTextView.text.toString()

            // Initialize Firebase database reference
            database = FirebaseDatabase.getInstance("https://capstoneproject-415506-default-rtdb.firebaseio.com/").getReference("IoTData")

            val user = IoTData(name,age,bpm,bodyTemp,airQuality,roomTemp,roomHumid)

            // Save data to Firebase database
            database.child(name).setValue(user).addOnSuccessListener {

                binding.etName.text.clear()
                binding.etAge.text.clear()

                Toast.makeText(this, "Successfully Saved", Toast.LENGTH_SHORT).show()

            }.addOnFailureListener{
                Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
            }
        }

        // Button to trigger analysis
        val analyzeButton: Button = findViewById(R.id.btnAnalyze)
        analyzeButton.setOnClickListener {
            onAnalyzeClicked()

            // Perform analysis only if the "Analyze" button was clicked
            if (shouldAnalyze) {
                lifecycleScope.launch { // Launch a coroutine using lifecycleScope
                    try {
                        // Extract BPM value
                        val bpmValue = bpmTextView.text.toString().toInt()

                        // Extract and clean room temperature and humidity
                        val roomTempString = roomTemperatureTextView.text.toString()
                        val roomTemp = roomTempString.replace(Regex("[^\\d.]"), "").toDoubleOrNull()

                        val roomHumidityString = roomHumidityTextView.text.toString()
                        val roomHumidity = roomHumidityString.replace(Regex("[^\\d.]"), "").toDoubleOrNull()

                        // Extract and clean body temperature and air quality
                        val bodyTempString = bodyTemperatureTextView.text.toString()
                        val bodyTemp = bodyTempString.replace(Regex("[^\\d.]"), "").toDoubleOrNull()

                        val airQualityString = airQualityTextView.text.toString()
                        val airQuality = airQualityString.replace(Regex("[^\\d.]"), "").toIntOrNull()

                        // Check if the conversions were successful
                        if (roomTemp != null && roomHumidity != null && bodyTemp != null && airQuality != null) {

                            val stringBuilder = StringBuilder()

                            // Call getOpenAIResponse with cleaned values
                            val response = getOpenAIResponse(bpmValue, roomTemp, roomHumidity, bodyTemp, airQuality)

                            // Append the analysis to the StringBuilder
                            stringBuilder.append(response)

                            // Convert StringBuilder to a regular String
                            val analysis = stringBuilder.toString()
                            responseAnalysis = analysis

                            Log.d("MainActivity", "OpenAI Response: $response")

                            // Display the analysis
                            displayAnalysis(responseAnalysis) {
                                // Add any action you want to perform after the analysis is displayed
                            }
                        } else {
                            // Handle conversion failure
                            Log.e("MainActivity", "Error: Unable to parse room temperature, humidity, body temperature, or air quality")
                        }
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Error: ${e.message}", e)
                        // Handle other exceptions
                    } finally {
                        shouldAnalyze = false // Reset the flag
                    }
                }
            }
        }

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        // Initialize TextViews for sensor data
        roomTemperatureTextView = findViewById(R.id.roomTemperatureTextView)
        roomHumidityTextView = findViewById(R.id.roomHumidityTextView)
        bodyTemperatureTextView = findViewById(R.id.bodyTemperatureTextView)
        airQualityTextView = findViewById(R.id.airQualityTextView)
        bpmTextView = findViewById(R.id.bpmTextView)

        // Schedule the data update task every 5 seconds (adjust as needed)
        handler.postDelayed(fetchDataTask, 5000)
    }


    // Task to fetch sensor data from the server
    private val fetchDataTask = object : Runnable {
        override fun run() {
            FetchData().execute()
            // Schedule the task again after the specified interval
            handler.postDelayed(this, 5000)
        }
    }

    // AsyncTask to fetch data from the server
    private inner class FetchData : AsyncTask<Void, Void, JSONObject>() {

        override fun doInBackground(vararg params: Void?): JSONObject? {
            try {

                val url = URL("http://192.168.1.15/data")  // Replace with your server IP
                val urlConnection = url.openConnection() as HttpURLConnection

                try {
                    val reader = BufferedReader(InputStreamReader(urlConnection.inputStream))
                    val response = StringBuilder()
                    var line: String?

                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }

                    return JSONObject(response.toString())
                } finally {
                    urlConnection.disconnect()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return null
        }

        override fun onPostExecute(result: JSONObject?) {
            super.onPostExecute(result)

            // Log the fetched JSON data
            Log.d("MainActivity", "Fetched data: $result")

            // Update UI with the fetched data
            if (result != null) {
                roomTemperatureTextView.text = " ${result.getDouble("roomTemperature")} °C"
                roomHumidityTextView.text = " ${result.getDouble("roomHumidity")} %"
                bodyTemperatureTextView.text = " ${result.getDouble("bodyTemperature")} °C"
                airQualityTextView.text = " ${result.getInt("airQuality")}%"
                bpmTextView.text = " ${result.getInt("ph")}"

                checkCondition(result.getInt("ph"))

            }
        }
    }

    private fun checkCondition(bpmValue: Int) {
        if (bpmValue != null) {
            if (bpmValue > 130 || bpmValue < 60) {
                callPhoneNumber("085710056837", this)
                sendMessage("+6285710056837", "Tolong, jantungku kambuh", this)
                sendEmail("rioprajna123@gmail.com", "Testing", "Ini hanya testing")
            }
        } else {
            Log.e("MainActivity", "Invalid BPM value: $bpmValue")
        }
    }

    private val openAIApi = ApiService.openAIApi

    private suspend fun getOpenAIResponse(bpmValue : Int, roomTemp : Double, roomHumid : Double, bodyTemp : Double, airQuality : Int): String {
        val prompt = generatePrompt(bpmValue, roomTemp, roomHumid, bodyTemp, airQuality)
        val response = openAIApi.generateResponse(OpenAIRequestBody(messages = listOf(Message(prompt, "user"))))
        return response.choices[0].message.content
    }

//    private suspend fun getOpenAIResponse(bpmValue: Int, roomTemp: Double, roomHumid: Double, bodyTemp: Double, airQuality: Int): String {
//        val prompt = generatePrompt(bpmValue, roomTemp, roomHumid, bodyTemp, airQuality)
//        val response = openAIApi.generateResponse(OpenAIRequestBody(messages = listOf(Message(prompt, "user"))))
//
//        val allResponses = response.choices.joinToString(separator = "") { it.message.content }
//        return allResponses
//    }


    private fun generatePrompt(bpmValue : Int, roomTemp : Double, roomHumid : Double, bodyTemp : Double, airQuality : Int): String {
        return "Hi there! I've gathered some vital data points about the user's current condition: " +
                "heart rate is " + bpmValue + ", room temperature is " + roomTemp + " Celcius, room humidity is " + roomHumid +
                " percent, " + "body temperature is " + bodyTemp + " Celcius, and air quality is " + airQuality + " percent. " +
                "Now, I need your expertise to analyze this information and provide detailed insights. " +
                "Can you please generate comprehensive analytics based on these parameters? " +
                "Specifically, I'm interested in understanding any correlations, potential health implications, " +
                "optimal ranges, and actionable recommendations. Your detailed analysis will greatly assist " +
                "in enhancing the user's well-being and comfort. Looking forward to your valuable insights!"
    }

    private suspend fun displayAnalysis(analysis: String, actionAfterAnalysis: () -> Unit) {
        val intent = Intent(this@MainActivity, AnalysisActivity::class.java)
        intent.putExtra("analysis", analysis)
        startActivity(intent)

        actionAfterAnalysis()
    }



    override fun onDestroy() {
        // Remove any remaining callbacks to prevent memory leaks
        handler.removeCallbacks(fetchDataTask)
        super.onDestroy()
    }
}
