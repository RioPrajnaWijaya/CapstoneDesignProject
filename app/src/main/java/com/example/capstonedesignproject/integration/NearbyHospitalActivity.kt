package com.example.capstonedesignproject.integration

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.capstonedesignproject.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient

class NearbyHospitalActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HospitalAdapter

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var placesClient: PlacesClient

    private val apiKey = "AIzaSyA0i_KIpsLP0sEbjKswLp_sGcqzGBukV0U" // Replace with your actual API key

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Location permission granted, start location updates
                fetchNearbyHospitals()
            } else {
                // Location permission denied
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nearby_hospital)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize adapter with empty list
        adapter = HospitalAdapter(emptyList())
        recyclerView.adapter = adapter

        // Initialize fusedLocationClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Initialize Places API with API key
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, apiKey)
        }
        placesClient = Places.createClient(this)

        // Request location permission if not granted
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission()
        } else {
            // Location permission already granted, fetch nearby hospitals
            fetchNearbyHospitals()
        }
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
    }

    private fun fetchNearbyHospitals() {
        val placeFields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)
        val request = FindCurrentPlaceRequest.builder(placeFields).build()

        // Request user's current place
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                // Use the Places API to find nearby hospitals
                placesClient.findCurrentPlace(request).addOnSuccessListener { response ->
                    val nearbyHospitals = mutableListOf<Hospital>()
                    for (placeLikelihood in response.placeLikelihoods) {
                        val place = placeLikelihood.place
                        // Check if the place is a hospital
                        if (place.types.contains(Place.Type.HOSPITAL)) {
                            // Add the hospital to the list
                            nearbyHospitals.add(Hospital(place.name ?: "", place.address ?: ""))
                        }
                    }
                    // Update RecyclerView with fetched hospitals
                    adapter.updateHospitals(nearbyHospitals)
                }.addOnFailureListener { e ->
                    // Handle Places API request failure
                    Toast.makeText(this, "Failed to get location: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("NearbyHospitalActivity", "${e.message}")
                }
            } else {
                // Handle null location
                Toast.makeText(this, "Failed to get location", Toast.LENGTH_SHORT).show()
            }
        }
    }
}





//package com.example.capstonedesignproject.integration
//
//import android.os.Bundle
//import androidx.appcompat.app.AppCompatActivity
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.example.capstonedesignproject.R
//
//class NearbyHospitalActivity : AppCompatActivity() {
//
//    private lateinit var recyclerView: RecyclerView
//    private lateinit var adapter: HospitalAdapter
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_nearby_hospital)
//
//        recyclerView = findViewById(R.id.recyclerView)
//        recyclerView.layoutManager = LinearLayoutManager(this)
//
//        // Initialize adapter with empty list
//        adapter = HospitalAdapter(emptyList())
//        recyclerView.adapter = adapter
//
//        // Fetch nearby hospitals and update RecyclerView
//        fetchNearbyHospitals()
//    }
//
//    private fun fetchNearbyHospitals() {
//        // Use Google Places API or other suitable API to fetch nearby hospitals
//        // Replace the code below with your implementation
//        val nearbyHospitals = listOf(
//            Hospital("Hospital A", "Address A"),
//            Hospital("Hospital B", "Address B"),
//            Hospital("Hospital C", "Address C")
//            // Add more hospitals as needed
//        )
//
//        // Update RecyclerView with fetched hospitals
//        adapter.updateHospitals(nearbyHospitals)
//    }
//}
