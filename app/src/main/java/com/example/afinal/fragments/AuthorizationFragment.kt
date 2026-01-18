package com.example.afinal.fragments

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.afinal.MainActivity
import com.example.afinal.R
import com.example.afinal.databinding.FragmentAuthorizationBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class AuthorizationFragment : Fragment() {

    private var _binding: FragmentAuthorizationBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private val TAG = "AuthorizationFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuthorizationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = Firebase.auth

        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Sign Up button (რეგისტრაცია) - Navigate to RegistrationFragment
        binding.btnSignUp.setOnClickListener {
            openRegistrationFragment()
        }

        // Log In button
        binding.btnLogIn.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (validateInput(email, password)) {
                loginUser(email, password)
            }
        }

        // Forgot password text - Navigate to PasswordChangeFragment
        binding.tvForgotPassword.setOnClickListener {
            openPasswordChangeFragment()
        }

        // Continue without login (optional)
        binding.tvContinueWithoutLogin.setOnClickListener {
            // Navigate to store without login
            (activity as? MainActivity)?.updateProfileNavigation()
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        var isValid = true

        // Validate email
        if (TextUtils.isEmpty(email)) {
            binding.etEmail.error = "გთხოვთ შეიყვანოთ ელფოსტა"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "გთხოვთ შეიყვანოთ სწორი ელფოსტა"
            isValid = false
        } else {
            binding.etEmail.error = null
        }

        // Validate password
        if (TextUtils.isEmpty(password)) {
            binding.etPassword.error = "გთხოვთ შეიყვანოთ პაროლი"
            isValid = false
        } else if (password.length < 6) {
            binding.etPassword.error = "პაროლი უნდა შეიცავდეს მინიმუმ 6 სიმბოლოს"
            isValid = false
        } else {
            binding.etPassword.error = null
        }

        return isValid
    }

    private fun openRegistrationFragment() {
        val registrationFragment = RegistrationFragment()
        (activity as? MainActivity)?.replaceFragment(registrationFragment)
    }

    private fun openPasswordChangeFragment() {
        val passwordChangeFragment = PasswordChangeFragment()
        (activity as? MainActivity)?.replaceFragment(passwordChangeFragment)
    }

    private fun loginUser(email: String, password: String) {
        binding.progressBar.visibility = View.VISIBLE

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                binding.progressBar.visibility = View.GONE

                if (task.isSuccessful) {
                    // Login success
                    Log.d(TAG, "signInWithEmail:success")

                    // Show success message
                    Toast.makeText(
                        requireContext(),
                        "წარმატებით შეხვედით!",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Navigate to profile
                    navigateToProfile()

                } else {
                    // Login failed
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(
                        requireContext(),
                        "ავტორიზაცია ვერ მოხერხდა: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
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