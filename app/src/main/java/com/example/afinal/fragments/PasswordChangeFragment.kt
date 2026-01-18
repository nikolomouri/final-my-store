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
import com.example.afinal.databinding.FragmentPasswordChangeBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class PasswordChangeFragment : Fragment() {

    private var _binding: FragmentPasswordChangeBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPasswordChangeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = Firebase.auth

        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Send reset link button
        binding.btnSendResetLink.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()

            if (validateEmail(email)) {
                sendPasswordResetEmail(email)
            }
        }

        // Back to login text
        binding.tvBackToLogin.setOnClickListener {
            // Go back to authorization fragment
            (activity as? MainActivity)?.supportFragmentManager?.popBackStack()
        }
    }

    private fun validateEmail(email: String): Boolean {
        if (TextUtils.isEmpty(email)) {
            binding.emailLayout.error = "გთხოვთ შეიყვანოთ ელფოსტა"
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailLayout.error = "გთხოვთ შეიყვანოთ სწორი ელფოსტა"
            return false
        }

        binding.emailLayout.error = null
        return true
    }

    private fun sendPasswordResetEmail(email: String) {
        binding.progressBar.visibility = View.VISIBLE

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                binding.progressBar.visibility = View.GONE

                if (task.isSuccessful) {
                    Toast.makeText(
                        requireContext(),
                        "პაროლის აღსადგენი ლინკი გამოგზავნილია ელფოსტაზე. გთხოვთ შეამოწმოთ თქვენი ელფოსტა და მიჰყვეთ ინსტრუქციას.",
                        Toast.LENGTH_LONG
                    ).show()

                    // Navigate back after successful send
                    view?.postDelayed({
                        navigateBackToAuthorization()
                    }, 3000)

                } else {
                    Toast.makeText(
                        requireContext(),
                        "ვერ მოხერხდა ლინკის გაგზავნა: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun navigateBackToAuthorization() {
        // Navigate back to authorization fragment
        (activity as? MainActivity)?.let {
            it.replaceFragment(AuthorizationFragment())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}