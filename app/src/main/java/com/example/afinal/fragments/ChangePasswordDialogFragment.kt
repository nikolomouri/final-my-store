package com.example.afinal.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.fragment.app.DialogFragment

import com.example.afinal.databinding.FragmentChangePasswordDialogBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class ChangePasswordDialogFragment : DialogFragment() {

    private lateinit var binding: FragmentChangePasswordDialogBinding
    private lateinit var auth: FirebaseAuth
    private var safeContext: Context? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        safeContext = context
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        auth = Firebase. auth

        binding = FragmentChangePasswordDialogBinding.inflate(layoutInflater)

        val builder = AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .setTitle("პაროლის შეცვლა")
            .setPositiveButton("შეცვლა", null) // Set to null initially
            .setNegativeButton("გაუქმება") { dialog, _ ->
                dialog.dismiss()
            }

        val dialog = builder.create()

        // We need to override the positive button click to prevent auto-dismiss
        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                changePassword(dialog)
            }
        }

        return dialog
    }

    private fun changePassword(dialog: AlertDialog) {
        val currentPassword = binding.etCurrentPassword.text.toString().trim()
        val newPassword = binding.etNewPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        // Validate inputs
        if (TextUtils.isEmpty(currentPassword)) {
            binding.currentPasswordLayout.error = "გთხოვთ შეიყვანოთ მიმდინარე პაროლი"
            return
        }

        if (TextUtils.isEmpty(newPassword)) {
            binding.newPasswordLayout.error = "გთხოვთ შეიყვანოთ ახალი პაროლი"
            return
        }

        if (newPassword.length < 6) {
            binding.newPasswordLayout.error = "ახალი პაროლი უნდა შეიცავდეს მინიმუმ 6 სიმბოლოს"
            return
        }

        if (newPassword != confirmPassword) {
            binding.confirmPasswordLayout.error = "პაროლები არ ემთხვევა"
            return
        }

        // Clear errors
        binding.currentPasswordLayout.error = null
        binding.newPasswordLayout.error = null
        binding.confirmPasswordLayout.error = null

        val user = auth.currentUser

        if (user != null && user.email != null) {
            // Disable the button to prevent multiple clicks
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.isEnabled = false

            // Re-authenticate user
            val credential = com.google.firebase.auth.EmailAuthProvider
                .getCredential(user.email!!, currentPassword)

            user.reauthenticate(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Update password
                        user.updatePassword(newPassword)
                            .addOnCompleteListener { updateTask ->
                                // Re-enable button
                                positiveButton.isEnabled = true

                                // Check if we can safely show Toast
                                if (safeContext != null && isAdded) {
                                    if (updateTask.isSuccessful) {
                                        Toast.makeText(
                                            safeContext,
                                            "პაროლი წარმატებით შეიცვალა",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                        // Dismiss dialog
                                        dialog.dismiss()
                                    } else {
                                        Toast.makeText(
                                            safeContext,
                                            "პაროლის შეცვლა ვერ მოხერხდა: ${updateTask.exception?.message}",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            }
                    } else {
                        // Re-enable button
                        positiveButton.isEnabled = true

                        // Check if we can safely show error
                        if (safeContext != null && isAdded) {
                            binding.currentPasswordLayout.error = "მიმდინარე პაროლი არასწორია"
                        }
                    }
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        safeContext = null
    }
}