package com.example.afinal.fragments

import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.afinal.MainActivity
import com.example.afinal.R
import com.example.afinal.databinding.FragmentRegistrationBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.FirebaseDatabase

class RegistrationFragment : Fragment() {

    private var _binding: FragmentRegistrationBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private val database = FirebaseDatabase.getInstance("https://final-ad852-default-rtdb.europe-west1.firebasedatabase.app")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegistrationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = Firebase.auth

        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Register button
        binding.btnRegister.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()
            val fullName = binding.etFullName.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()

            if (validateInput(email, password, confirmPassword, fullName, phone)) {
                registerUser(email, password, fullName, phone)
            }
        }

        // Back to login text - FIXED
        binding.tvBackToLogin.setOnClickListener {
            navigateBackToAuthorization()
        }
    }

    private fun validateInput(
        email: String,
        password: String,
        confirmPassword: String,
        fullName: String,
        phone: String
    ): Boolean {
        var isValid = true

        // Validate email
        if (TextUtils.isEmpty(email)) {
            binding.emailLayout.error = "გთხოვთ შეიყვანოთ ელფოსტა"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailLayout.error = "გთხოვთ შეიყვანოთ სწორი ელფოსტა"
            isValid = false
        } else {
            binding.emailLayout.error = null
        }

        // Validate full name
        if (TextUtils.isEmpty(fullName)) {
            binding.fullNameLayout.error = "გთხოვთ შეიყვანოთ სრული სახელი"
            isValid = false
        } else {
            binding.fullNameLayout.error = null
        }

        // Validate phone
        if (TextUtils.isEmpty(phone)) {
            binding.phoneLayout.error = "გთხოვთ შეიყვანოთ ტელეფონის ნომერი"
            isValid = false
        } else if (phone.length < 9) {
            binding.phoneLayout.error = "ტელეფონის ნომერი არასწორია"
            isValid = false
        } else {
            binding.phoneLayout.error = null
        }

        // Validate password
        if (TextUtils.isEmpty(password)) {
            binding.passwordLayout.error = "გთხოვთ შეიყვანოთ პაროლი"
            isValid = false
        } else if (password.length < 6) {
            binding.passwordLayout.error = "პაროლი უნდა შეიცავდეს მინიმუმ 6 სიმბოლოს"
            isValid = false
        } else {
            binding.passwordLayout.error = null
        }

        // Validate confirm password
        if (TextUtils.isEmpty(confirmPassword)) {
            binding.confirmPasswordLayout.error = "გთხოვთ გაიმეოროთ პაროლი"
            isValid = false
        } else if (password != confirmPassword) {
            binding.confirmPasswordLayout.error = "პაროლები არ ემთხვევა"
            isValid = false
        } else {
            binding.confirmPasswordLayout.error = null
        }

        return isValid
    }

    private fun registerUser(email: String, password: String, fullName: String, phone: String) {
        binding.progressBar.visibility = View.VISIBLE

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                binding.progressBar.visibility = View.GONE

                if (task.isSuccessful) {
                    // Registration success
                    val user = auth.currentUser
                    user?.let {
                        // Save additional user data to Firebase Database
                        saveUserToDatabase(it.uid, email, fullName, phone)

                        // Update display name in Firebase Auth
                        val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                            .setDisplayName(fullName)
                            .build()

                        it.updateProfile(profileUpdates)
                            .addOnCompleteListener { updateTask ->
                                if (updateTask.isSuccessful) {
                                    // Show success message
                                    Toast.makeText(
                                        requireContext(),
                                        "რეგისტრაცია წარმატებით დასრულდა!",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    // Navigate directly to store and then to profile
                                    navigateToProfile()
                                }
                            }
                    }
                } else {
                    // Registration failed
                    Toast.makeText(
                        requireContext(),
                        "რეგისტრაცია ვერ მოხერხდა: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun saveUserToDatabase(userId: String, email: String, fullName: String, phone: String) {
        val userData = hashMapOf<String, Any>(
            "userId" to userId,
            "email" to email,
            "fullName" to fullName,
            "phone" to phone,
            "createdAt" to System.currentTimeMillis(),
            "role" to "user"
        )

        database.getReference("users").child(userId).setValue(userData)
    }

    private fun navigateBackToAuthorization() {
        // Method 1: Use activity's onBackPressed
        activity?.onBackPressed()

        // Method 2: Directly replace fragment
        // (activity as? MainActivity)?.replaceFragment(AuthorizationFragment())

        // Method 3: Pop back stack
        // parentFragmentManager.popBackStack()
    }

    private fun navigateToProfile() {
        // Navigate to profile fragment
        (activity as? MainActivity)?.let {
            it.replaceFragment(ProfileFragment())
            val bottomNav = it.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)
            bottomNav.selectedItemId = R.id.navigation_profile
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}