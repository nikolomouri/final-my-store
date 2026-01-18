package com.example.afinal.utils

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.afinal.CartItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.example.afinal.Product

object CartManager {

    private val TAG = "CartManager"
    private val database = FirebaseDatabase.getInstance("https://final-ad852-default-rtdb.europe-west1.firebasedatabase.app")
    private val auth = FirebaseAuth.getInstance()

    fun addToCart(context: Context, product: Product) {
        Log.d(TAG, "addToCart called for product: ${product.id}")

        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.e(TAG, "User not logged in!")
            Toast.makeText(context, "Please login to add items to cart", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d(TAG, "User ID: $userId, Product ID: ${product.id}")

        val cartRef = database.getReference("carts")
            .child(userId)
            .child("items")
            .child(product.id)

        // Check if product already in cart
        cartRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                // Update quantity
                val currentQuantity = snapshot.child("quantity").getValue(Int::class.java) ?: 0
                val newQuantity = currentQuantity + 1
                cartRef.child("quantity").setValue(newQuantity)
                    .addOnSuccessListener {
                        Log.d(TAG, "Quantity updated to $newQuantity")
                        Toast.makeText(context, "Quantity updated in cart", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Failed to update quantity: ${e.message}")
                    }
            } else {
                // Add new item
                val cartItem = CartItem(productId = product.id, quantity = 1)
                cartRef.setValue(cartItem)
                    .addOnSuccessListener {
                        Log.d(TAG, "Product added to cart successfully")
                        Toast.makeText(context, "Added to cart", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Failed to add to cart: ${e.message}")
                        Toast.makeText(context, "Failed to add to cart: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }.addOnFailureListener { e ->
            Log.e(TAG, "Failed to check cart: ${e.message}")
            Toast.makeText(context, "Failed to check cart", Toast.LENGTH_SHORT).show()
        }
    }

    fun removeFromCart(context: Context, productId: String) {
        val userId = auth.currentUser?.uid ?: return

        val cartRef = database.getReference("carts")
            .child(userId)
            .child("items")
            .child(productId)

        cartRef.removeValue()
            .addOnSuccessListener {
                Toast.makeText(context, "Removed from cart", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to remove: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    fun updateCartItemQuantity(context: Context, productId: String, newQuantity: Int) {
        val userId = auth.currentUser?.uid ?: return

        val cartRef = database.getReference("carts")
            .child(userId)
            .child("items")
            .child(productId)

        if (newQuantity <= 0) {
            removeFromCart(context, productId)
        } else {
            cartRef.child("quantity").setValue(newQuantity)
                .addOnSuccessListener {
                    Log.d(TAG, "Quantity updated to $newQuantity for product $productId")
                }
        }
    }

    fun clearCart() {
        val userId = auth.currentUser?.uid ?: return

        val cartRef = database.getReference("carts").child(userId)
        cartRef.removeValue()
    }
}