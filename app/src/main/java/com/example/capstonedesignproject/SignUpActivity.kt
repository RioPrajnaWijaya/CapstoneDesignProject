package com.example.capstonedesignproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import com.example.capstonedesignproject.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize view binding
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        binding.btnSignUp.setOnClickListener{
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            if (checkAllField()){
                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                    // If account creation is successful, sign out and navigate to sign-in activity
                    if (it.isSuccessful){
                        auth.signOut()
                        Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, SignInActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    else{
                        // If account creation fails, log the error
                        Log.e("error: ", it.exception.toString())
                    }
                }
            }
        }

        // Navigate to sign-in activity when "Have an account?" is clicked
        binding.haveAccount.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    // Check if all input fields are filled and meet the required criteria
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
        if (binding.etConfirmPassword.text.toString() == ""){
            binding.textInputLayoutConfirmPassword.error = "This is required field"
            binding.textInputLayoutConfirmPassword.errorIconDrawable = null
            return false
        }
        if (binding.etPassword.text.toString() != binding.etConfirmPassword.text.toString()){
            binding.textInputLayoutPassword.error = "Password do not match"
            return false
        }
        return true

    }
}
