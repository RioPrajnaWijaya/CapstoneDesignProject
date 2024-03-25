package com.example.capstonedesignproject.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.capstonedesignproject.IoTData
import com.example.capstonedesignproject.R

class MyAdapter(private val iotData: ArrayList<IoTData>) :
    RecyclerView.Adapter<MyAdapter.MyViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.iot_data, parent, false)

        return MyViewHolder(itemView)
    }

    override fun getItemCount(): Int {

        return iotData.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        val currentData = iotData[position]

        holder.getName.text = currentData.name
        holder.getAge.text = currentData.age
        holder.getBpm.text = currentData.bpm
        holder.getRoomTemp.text = currentData.roomTemp
        holder.getRoomHumid.text = currentData.roomHumid
        holder.getBodyTemp.text = currentData.bodyTemp
        holder.getAirQuality.text = currentData.airQuality

    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val getName : TextView = itemView.findViewById(R.id.setname)
        val getAge : TextView = itemView.findViewById(R.id.setage)
        val getBpm : TextView = itemView.findViewById(R.id.setbpm)
        val getRoomTemp : TextView = itemView.findViewById(R.id.setroomtemp)
        val getRoomHumid : TextView = itemView.findViewById(R.id.setroomhumid)
        val getBodyTemp : TextView = itemView.findViewById(R.id.setbodytemp)
        val getAirQuality : TextView = itemView.findViewById(R.id.setairquality)
    }
}