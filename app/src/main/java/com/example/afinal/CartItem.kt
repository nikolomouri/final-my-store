package com.example.afinal

data class CartItem(
    var productId: String = "",
    var quantity: Int = 1,
    val addedAt: Long = System.currentTimeMillis()
) {
    // Add a no-argument constructor for Firebase
    constructor() : this("", 1, System.currentTimeMillis())
}