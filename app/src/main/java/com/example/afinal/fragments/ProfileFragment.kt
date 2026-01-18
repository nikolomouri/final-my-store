package com.example.afinal.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.afinal.MainActivity
import com.example.afinal.R
import com.example.afinal.databinding.FragmentProfileBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.FirebaseDatabase

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private val database = FirebaseDatabase.getInstance("https://final-ad852-default-rtdb.europe-west1.firebasedatabase.app")

    // Store user data when fragment is active
    private var currentUserUid: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = Firebase.auth

        // Check if user is logged in
        val currentUser = auth.currentUser
        if (currentUser == null) {
            // User not logged in, go back to authorization
            (activity as? MainActivity)?.let {
                it.replaceFragment(AuthorizationFragment())
            }
            return
        }

        currentUserUid = currentUser.uid

        setupUserInfo(currentUser)
        setupClickListeners()
        loadAdditionalUserInfo(currentUser.uid)
    }

    private fun setupUserInfo(currentUser: com.google.firebase.auth.FirebaseUser) {
        // Set welcome message with user's display name or email
        val displayName = currentUser.displayName ?: currentUser.email?.split("@")?.get(0) ?: "მომხმარებელი"
        binding.tvWelcome.text = "კეთილი იყოს თქვენი მობრძანება, $displayName!"

        // Set user email
        binding.tvEmail.text = currentUser.email ?: "ელფოსტა არ არის მითითებული"
    }

    private fun loadAdditionalUserInfo(userId: String) {
        // Check if fragment is still attached and view exists
        if (!isAdded || _binding == null) {
            return
        }

        val userRef = database.getReference("users").child(userId)

        userRef.get().addOnSuccessListener { snapshot ->
            // Check again if fragment is still attached
            if (!isAdded || _binding == null) {
                return@addOnSuccessListener
            }

            if (snapshot.exists()) {
                val fullName = snapshot.child("fullName").getValue(String::class.java)
                val phone = snapshot.child("phone").getValue(String::class.java)

                fullName?.let {
                    binding.tvFullName.text = it
                    binding.tvFullName.visibility = View.VISIBLE
                }

                phone?.let {
                    binding.tvPhone.text = it
                    binding.tvPhone.visibility = View.VISIBLE
                }
            }
        }.addOnFailureListener {
            // User data doesn't exist in database, that's okay
            if (isAdded && _binding != null) {
                // Handle error if needed
            }
        }
    }

    private fun setupClickListeners() {
        // Change Password button
        binding.btnChangePassword.setOnClickListener {
            showChangePasswordDialog()
        }

        // Edit Profile button
        binding.btnEditProfile.setOnClickListener {
            showEditProfileDialog()
        }

        // Logout button
        binding.btnLogout.setOnClickListener {
            logoutUser()
        }

        // Order History button
       // binding.btnOrderHistory.setOnClickListener {
            // Navigate to order history
      //      Toast.makeText(requireContext(), "შეკვეთების ისტორია", Toast.LENGTH_SHORT).show()
      //  }

        // My Addresses button
      //  binding.btnMyAddresses.setOnClickListener {
            // Navigate to addresses
        //    Toast.makeText(requireContext(), "ჩემი მისამართები", Toast.LENGTH_SHORT).show()
       // }
    }

    private fun showChangePasswordDialog() {
        val dialog = ChangePasswordDialogFragment()
        dialog.show(childFragmentManager, "ChangePasswordDialog")
    }

    private fun showEditProfileDialog() {
        val dialog = EditProfileDialogFragment()
        dialog.show(childFragmentManager, "EditProfileDialog")
    }

    private fun logoutUser() {
        auth.signOut()
        Toast.makeText(requireContext(), "გამოსვლა წარმატებით დასრულდა", Toast.LENGTH_SHORT).show()

        // Navigate back to authorization
        (activity as? MainActivity)?.let {
            it.replaceFragment(AuthorizationFragment())
            it.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation).selectedItemId = R.id.navigation_store
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clear the binding when view is destroyed
        _binding = null
    }

    companion object {
        fun newInstance(): ProfileFragment {
            return ProfileFragment()
        }
    }
}