package com.example.afinal

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import java.text.DecimalFormat

class ProductAdapter(
    private val products: List<Product>,
    private val onAddToCart: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    private val TAG = "ProductAdapter"

    // Create a RequestOptions object to reuse
    private val requestOptions = RequestOptions()
        .placeholder(R.drawable.placeholder_image)
        .error(R.drawable.error_image)
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .centerCrop()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]
        holder.bind(product)
    }

    override fun getItemCount(): Int = products.size

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val productImage: ImageView = itemView.findViewById(R.id.product_image)
        private val productName: TextView = itemView.findViewById(R.id.product_name)
        private val productPrice: TextView = itemView.findViewById(R.id.product_price)
        private val addToCartBtn: Button = itemView.findViewById(R.id.add_to_cart_button)

        fun bind(product: Product) {
            productName.text = product.name

            val decimalFormat = DecimalFormat("#.##")
            productPrice.text = "${decimalFormat.format(product.price)}₾"

            // Debug: Log the image URL
            Log.d(TAG, "Loading image for ${product.name}: ${product.imageUrl}")

            // Load image with Glide - Fixed version
            try {
                // Check if URL is valid
                if (product.imageUrl.isNotEmpty() &&
                    (product.imageUrl.startsWith("http://") || product.imageUrl.startsWith("https://"))) {

                    Glide.with(itemView.context)
                        .load(product.imageUrl)
                        .apply(requestOptions)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(productImage)

                    Log.d(TAG, "Glide load initiated for: ${product.name}")
                } else {
                    // Use placeholder if URL is invalid
                    Log.w(TAG, "Invalid image URL for ${product.name}: ${product.imageUrl}")
                    productImage.setImageResource(R.drawable.placeholder_image)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading image for ${product.name}: ${e.message}")
                productImage.setImageResource(R.drawable.error_image)
            }

            addToCartBtn.setOnClickListener {
                Log.d(TAG, "Add to cart clicked for product: ${product.id} - ${product.name}")
                onAddToCart(product)
            }
        }
    }
}