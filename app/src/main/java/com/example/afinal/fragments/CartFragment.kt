package com.example.afinal.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.afinal.CartAdapter
import com.example.afinal.CartItem
import com.example.afinal.Product
import com.example.afinal.databinding.FragmentCartBinding
import com.example.afinal.utils.CartManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.DecimalFormat

class CartFragment : Fragment() {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

    private lateinit var cartAdapter: CartAdapter
    private val cartItems = mutableListOf<CartItem>()
    private val products = mutableMapOf<String, Product>() // Changed to Map for faster lookup
    private val database = FirebaseDatabase.getInstance("https://final-ad852-default-rtdb.europe-west1.firebasedatabase.app")
    private val auth = FirebaseAuth.getInstance()

    // Listener references to remove when fragment is destroyed
    private var cartListener: ValueEventListener? = null
    private val productListeners = mutableListOf<ValueEventListener>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupCheckoutButton()
        loadCartItems()
    }

    private fun setupRecyclerView() {
        cartAdapter = CartAdapter(cartItems, products) { cartItem, operation ->
            when (operation) {
                "increase" -> CartManager.updateCartItemQuantity(
                    requireContext(),
                    cartItem.productId,
                    cartItem.quantity + 1
                )
                "decrease" -> {
                    if (cartItem.quantity > 1) {
                        CartManager.updateCartItemQuantity(
                            requireContext(),
                            cartItem.productId,
                            cartItem.quantity - 1
                        )
                    } else {
                        CartManager.removeFromCart(requireContext(), cartItem.productId)
                    }
                }
                "remove" -> CartManager.removeFromCart(requireContext(), cartItem.productId)
            }
        }

        binding.cartRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = cartAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupCheckoutButton() {
        binding.checkoutButton.setOnClickListener {
            if (cartItems.isNotEmpty()) {
                // Handle checkout process
                // You can implement payment gateway integration here
            }
        }
    }

    private fun loadCartItems() {
        val userId = auth.currentUser?.uid ?: return

        val cartRef = database.getReference("carts").child(userId).child("items")

        // Remove previous listener if exists
        cartListener?.let { cartRef.removeEventListener(it) }

        cartListener = cartRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                cartItems.clear()
                products.clear()
                // Clear previous product listeners
                productListeners.clear()

                if (!snapshot.exists()) {
                    showEmptyCart()
                    return
                }

                binding.emptyCartText.visibility = View.GONE

                // Count of items to load
                val itemCount = snapshot.childrenCount.toInt()
                var loadedCount = 0

                if (itemCount == 0) {
                    showEmptyCart()
                    return
                }

                for (itemSnapshot in snapshot.children) {
                    val cartItem = itemSnapshot.getValue(CartItem::class.java)
                    cartItem?.let {
                        it.productId = itemSnapshot.key ?: ""
                        cartItems.add(it)

                        // Fetch product details
                        fetchProductDetails(it.productId) { product ->
                            loadedCount++
                            if (loadedCount == itemCount) {
                                // All products loaded, update UI
                                cartAdapter.updateData(cartItems, products)
                                updateTotalPrice()
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
                showEmptyCart()
            }
        })
    }

    private fun fetchProductDetails(productId: String, onComplete: (Product?) -> Unit = {}) {
        // Search product in all categories
        val categories = listOf("servers", "nas_servers", "network", "server_components")

        // Track if product is found
        var productFound = false

        for (category in categories) {
            if (productFound) break

            val productRef = database.getReference("products")
                .child(category)
                .child(productId)

            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists() && !productFound) {
                        productFound = true
                        val product = snapshot.getValue(Product::class.java)
                        product?.let {
                            it.id = productId
                            products[productId] = it
                            onComplete(it)
                        } ?: run {
                            onComplete(null)
                        }

                        // Remove this listener after getting the product
                        productRef.removeEventListener(this)
                    } else {
                        // Product not found in this category
                        if (category == categories.last() && !productFound) {
                            // Product not found in any category
                            onComplete(null)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    onComplete(null)
                }
            }

            productRef.addListenerForSingleValueEvent(listener)
            productListeners.add(listener)
        }

        // If no categories (shouldn't happen), call onComplete
        if (categories.isEmpty()) {
            onComplete(null)
        }
    }

    private fun updateTotalPrice() {
        var total = 0.0

        for (cartItem in cartItems) {
            val product = products[cartItem.productId]
            product?.let {
                total += it.price * cartItem.quantity
            }
        }

        val decimalFormat = DecimalFormat("#.##")
        binding.totalPriceText.text = "${decimalFormat.format(total)}₾"

        // Update checkout button state
        binding.checkoutButton.isEnabled = total > 0
    }

    private fun showEmptyCart() {
        binding.emptyCartText.visibility = View.VISIBLE
        binding.totalPriceText.text = "0.00₾"
        binding.checkoutButton.isEnabled = false
        cartItems.clear()
        products.clear()
        cartAdapter.updateData(cartItems, products)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        // Remove all Firebase listeners
        val userId = auth.currentUser?.uid
        userId?.let {
            cartListener?.let { listener ->
                database.getReference("carts").child(it).child("items")
                    .removeEventListener(listener)
            }
        }

        _binding = null
    }
}