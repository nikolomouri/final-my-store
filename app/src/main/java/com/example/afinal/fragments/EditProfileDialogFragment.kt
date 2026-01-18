package com.example.afinal.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.afinal.databinding.FragmentEditProfileDialogBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.FirebaseDatabase

class EditProfileDialogFragment : DialogFragment() {

    private lateinit var binding: FragmentEditProfileDialogBinding
    private lateinit var auth: FirebaseAuth
    private val database = FirebaseDatabase.getInstance("https://final-ad852-default-rtdb.europe-west1.firebasedatabase.app")

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        auth = Firebase.auth

        binding = FragmentEditProfileDialogBinding.inflate(LayoutInflater.from(requireContext()))

        val user = auth.currentUser
        user?.let {
            binding.etEmail.setText(it.email ?: "")

            // Load existing user data
            loadUserData(it.uid)
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .setTitle("პროფილის რედაქტირება")
            .setPositiveButton("შენახვა", null) // Set to null, we'll handle click
            .setNegativeButton("გაუქმება") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                saveProfile(dialog)
            }
        }

        return dialog
    }

    private fun loadUserData(userId: String) {
        val userRef = database.getReference("users").child(userId)

        userRef.get().addOnSuccessListener { snapshot ->
            // Check if fragment is still attached and view exists
            if (view == null || !isAdded) {
                return@addOnSuccessListener
            }

            if (snapshot.exists()) {
                val fullName = snapshot.child("fullName").getValue(String::class.java)
                val phone = snapshot.child("phone").getValue(String::class.java)

                fullName?.let { binding.etFullName.setText(it) }
                phone?.let { binding.etPhone.setText(it) }
            }
        }
    }

    private fun saveProfile(dialog: AlertDialog) {
        val user = auth.currentUser
        user?.let {
            val fullName = binding.etFullName.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()

            // Validate phone number if provided
            if (phone.isNotEmpty() && phone.length < 9) {
                binding.phoneLayout.error = "ტელეფონის ნომერი არასწორია"
                return
            }

            // Clear errors
            binding.phoneLayout.error = null

            // Disable button to prevent multiple clicks
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.isEnabled = false

            val userData = hashMapOf<String, Any>(
                "fullName" to fullName,
                "phone" to phone,
                "email" to (user.email ?: "")
            )

            database.getReference("users").child(user.uid).setValue(userData)
                .addOnSuccessListener {
                    // Re-enable button
                    positiveButton.isEnabled = true

                    // Check if we can safely show Toast
                    if (view == null || !isAdded) {
                        dialog.dismiss()
                        return@addOnSuccessListener
                    }

                    Toast.makeText(
                        requireContext(),
                        "პროფილი წარმატებით შეინახა",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Update display name in Firebase Auth
                    if (fullName.isNotEmpty()) {
                        val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                            .setDisplayName(fullName)
                            .build()

                        user.updateProfile(profileUpdates)
                    }

                    dialog.dismiss()
                }
                .addOnFailureListener { e ->
                    // Re-enable button
                    positiveButton.isEnabled = true

                    // Check if we can safely show Toast
                    if (view == null || !isAdded) {
                        return@addOnFailureListener
                    }

                    Toast.makeText(
                        requireContext(),
                        "შეცდომა: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
    }
}