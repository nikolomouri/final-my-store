package com.example.afinal

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.afinal.fragments.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        setupBottomNavigation()

        // Check if user is logged in
        checkUserAuth()
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNav.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_store -> {
                    replaceFragment(StoreFragment())
                    true
                }
                R.id.navigation_cart -> {
                    if (auth.currentUser != null) {
                        replaceFragment(CartFragment())
                    } else {
                        replaceFragment(AuthorizationFragment())
                    }
                    true
                }
                R.id.navigation_profile -> {
                    if (auth.currentUser != null) {
                        replaceFragment(ProfileFragment())
                    } else {
                        replaceFragment(AuthorizationFragment())
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun checkUserAuth() {
        if (auth.currentUser == null) {
            // User not logged in, show store but set default selection to store
            replaceFragment(StoreFragment())
            val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
            bottomNav.selectedItemId = R.id.navigation_store
        } else {
            // User is logged in, show store as default
            replaceFragment(StoreFragment())
        }
    }

    fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(fragment::class.java.simpleName) // Add to back stack with a tag
            .commit()
    }

    fun updateProfileNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.navigation_store
    }

    override fun onBackPressed() {
        // Handle back button press
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)

        when (currentFragment) {
            is StoreFragment -> {
                // If we're in StoreFragment and back stack is empty, exit app
                if (supportFragmentManager.backStackEntryCount <= 1) {
                    finish()
                } else {
                    super.onBackPressed()
                }
            }
            is AuthorizationFragment -> {
                // Go to StoreFragment instead of exiting
                replaceFragment(StoreFragment())
                val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
                bottomNav.selectedItemId = R.id.navigation_store
            }
            else -> {
                super.onBackPressed()
            }
        }
    }
}