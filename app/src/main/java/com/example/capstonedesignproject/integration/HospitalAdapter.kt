package com.example.capstonedesignproject.integration

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.capstonedesignproject.R

class HospitalAdapter(private var hospitals: List<Hospital>) :
    RecyclerView.Adapter<HospitalAdapter.HospitalViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HospitalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_hospital, parent, false)
        return HospitalViewHolder(view)
    }

    override fun onBindViewHolder(holder: HospitalViewHolder, position: Int) {
        val hospital = hospitals[position]
        holder.nameTextView.text = hospital.name
        holder.addressTextView.text = hospital.address
    }

    override fun getItemCount(): Int {
        return hospitals.size
    }

    fun updateHospitals(newHospitals: List<Hospital>) {
        hospitals = newHospitals
        notifyDataSetChanged()
    }

    class HospitalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        val addressTextView: TextView = itemView.findViewById(R.id.addressTextView)
    }
}

data class Hospital(val name: String, val address: String)
