package com.example.capstonedesignproject.history

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.capstonedesignproject.IoTData
import com.example.capstonedesignproject.R
import com.google.firebase.database.*

class HistoryActivity : AppCompatActivity() {

    private lateinit var dbref : DatabaseReference
    private lateinit var userRecyclerView: RecyclerView
    private lateinit var dataArrayList : ArrayList<IoTData>
    private lateinit var adapter: MyAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        // Initialize RecyclerView
        userRecyclerView = findViewById(R.id.iotdata)
        userRecyclerView.layoutManager = LinearLayoutManager(this)
        userRecyclerView.setHasFixedSize(true)

        // Initialize data ArrayList and adapter
        dataArrayList = arrayListOf()
        adapter = MyAdapter(dataArrayList)
        userRecyclerView.adapter = adapter

        getIoTData()
    }

    // Retrieves IoT data from Firebase Realtime Database and populates the RecyclerView
    private fun getIoTData() {
        dbref = FirebaseDatabase.getInstance().getReference("IoTData")

        // Create a Query to order the data by a child key in descending order
        val query = dbref.orderByKey().limitToLast(50) // Adjust limit as needed

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val dataList = mutableListOf<IoTData>()
                    for (dataSnapshot in snapshot.children.reversed()) { // Iterate in reverse order
                        val name = dataSnapshot.child("name").getValue(String::class.java) ?: ""
                        val age = dataSnapshot.child("age").getValue(String::class.java) ?: ""
                        val bpm = dataSnapshot.child("bpm").getValue(String::class.java) ?: ""
                        val roomTemp = dataSnapshot.child("roomTemp").getValue(String::class.java) ?: ""
                        val roomHumid = dataSnapshot.child("roomHumid").getValue(String::class.java) ?: ""
                        val bodyTemp = dataSnapshot.child("bodyTemp").getValue(String::class.java) ?: ""
                        val airQuality = dataSnapshot.child("airQuality").getValue(String::class.java) ?: ""
                        val data = IoTData(name, age, bpm, roomTemp, roomHumid, bodyTemp, airQuality)
                        dataList.add(data)
                    }
                    updateRecyclerView(dataList)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("HistoryActivity", "Firebase Database Error: ${error.message}")
            }
        })
    }



    // Update RecyclerView adapter with new data on the main thread
    private fun updateRecyclerView(dataList: List<IoTData>) {
        runOnUiThread {
            dataArrayList.clear()
            dataArrayList.addAll(dataList)
            adapter.notifyDataSetChanged()
        }
    }
}
