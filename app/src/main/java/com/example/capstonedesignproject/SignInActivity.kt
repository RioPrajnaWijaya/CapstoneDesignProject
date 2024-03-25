package com.example.capstonedesignproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import com.example.capstonedesignproject.databinding.ActivitySignInBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SignInActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivitySignInBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize view binding
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Use to remove / hide action bar
        supportActionBar?.hide()

        // Initialize Firebase authentication
        auth = Firebase.auth

        // Sign in button click listener
        binding.btnSignIn.setOnClickListener{
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            if (checkAllField()){
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                    if (it.isSuccessful){
                        // If successful already sign in
                        Toast.makeText(this,"Successfully Sign In", Toast.LENGTH_SHORT).show()
                        // Go to another activity
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }else{
                        // Not sign in
                        Log.e("error : ", it.exception.toString())
                    }
                }
            }
        }

        // Don't have an account button click listener
        binding.DHAccount.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    // Check if all required fields are filled and in correct format
    private fun checkAllField(): Boolean{
        var email = binding.etEmail.text.toString()
        if(binding.etEmail.text.toString() == ""){
            binding.textInputLayoutEmail.error = "This is required field"
            return false
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.textInputLayoutEmail.error = "Check email Format"
            return false
        }
        if (binding.etPassword.text.toString() == ""){
            binding.textInputLayoutPassword.error = "This is required field"
            binding.textInputLayoutPassword.errorIconDrawable = null
            return false
        }
        if (binding.etPassword.length() <= 6){
            binding.textInputLayoutPassword.error = "Password should at least 8 Character"
            binding.textInputLayoutPassword.errorIconDrawable = null
            return false
        }
        return true
    }
}