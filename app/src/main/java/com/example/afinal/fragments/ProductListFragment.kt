package com.example.afinal.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.afinal.Product
import com.example.afinal.ProductAdapter
import com.example.afinal.databinding.FragmentProductListBinding
import com.example.afinal.utils.CartManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProductListFragment : Fragment() {

    private var _binding: FragmentProductListBinding? = null
    private val binding get() = _binding!!
    private lateinit var productAdapter: ProductAdapter
    private val products = mutableListOf<Product>()
    private lateinit var categoryId: String
    private val database = FirebaseDatabase.getInstance("https://final-ad852-default-rtdb.europe-west1.firebasedatabase.app")
    private val TAG = "ProductListFragment"

    companion object {
        private const val ARG_CATEGORY_ID = "category_id"

        fun newInstance(categoryId: String): ProductListFragment {
            val fragment = ProductListFragment()
            val args = Bundle()
            args.putString(ARG_CATEGORY_ID, categoryId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        categoryId = arguments?.getString(ARG_CATEGORY_ID) ?: "servers"
        Log.d(TAG, "Fragment created for category: $categoryId")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        testFirebaseConnection()
        loadProductsFromFirebase()
    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter(products) { product ->
            CartManager.addToCart(requireContext(), product)
        }

        binding.productsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = productAdapter
            setHasFixedSize(true)
        }
    }

    private fun testFirebaseConnection() {
        Log.d(TAG, "Testing Firebase connection...")

        // Test if Firebase is initialized
        try {
            val testRef = database.reference
            Log.d(TAG, "Firebase Database reference created successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Firebase initialization error: ${e.message}")
        }
    }

    private fun loadProductsFromFirebase() {
        binding.progressBar.visibility = View.VISIBLE
        Log.d(TAG, "Loading products for category: $categoryId")

        // Try to read from database
        val productsRef = database.getReference("products").child(categoryId)
        Log.d(TAG, "Database path: ${productsRef.path}")

        productsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d(TAG, "onDataChange called. Has data: ${snapshot.exists()}")

                if (!snapshot.exists()) {
                    Log.d(TAG, "No products found for category: $categoryId")
                    binding.progressBar.visibility = View.GONE
                    return
                }

                Log.d(TAG, "Found ${snapshot.childrenCount} products")

                products.clear()
                for (productSnapshot in snapshot.children) {
                    Log.d(TAG, "Product snapshot: ${productSnapshot.key}")
                    val product = productSnapshot.getValue(Product::class.java)

                    product?.let {
                        it.id = productSnapshot.key ?: ""
                        Log.d(TAG, "Product loaded: ${it.name}, Price: ${it.price}")
                        products.add(it)
                    } ?: run {
                        Log.d(TAG, "Failed to parse product: ${productSnapshot.key}")
                    }
                }

                productAdapter.notifyDataSetChanged()
                binding.progressBar.visibility = View.GONE

                // Show message if no products
                if (products.isEmpty()) {
                    Log.d(TAG, "No products could be parsed from the database")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Firebase database error: ${error.message}")
                binding.progressBar.visibility = View.GONE
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}