package com.example.afinal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.text.DecimalFormat

class CartAdapter(
    private var cartItems: MutableList<CartItem>,
    private var products: Map<String, Product>,
    private val onCartItemChanged: (CartItem, String) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    fun updateData(newCartItems: MutableList<CartItem>, newProducts: Map<String, Product>) {
        this.cartItems = newCartItems
        this.products = newProducts
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart_product, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val cartItem = cartItems[position]
        holder.bind(cartItem, products[cartItem.productId])
    }

    override fun getItemCount(): Int = cartItems.size

    inner class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val productImage: ImageView = itemView.findViewById(R.id.cart_product_image)
        private val productName: TextView = itemView.findViewById(R.id.cart_product_name)
        private val productPrice: TextView = itemView.findViewById(R.id.cart_product_price)
        private val quantityText: TextView = itemView.findViewById(R.id.quantity_text)
        private val decreaseBtn: Button = itemView.findViewById(R.id.quantity_decrease)
        private val increaseBtn: Button = itemView.findViewById(R.id.quantity_increase)
        private val removeBtn: Button = itemView.findViewById(R.id.remove_button)

        fun bind(cartItem: CartItem, product: Product?) {
            product?.let {
                productName.text = it.name

                val decimalFormat = DecimalFormat("#.##")
                val totalPrice = it.price * cartItem.quantity
                productPrice.text = "${decimalFormat.format(totalPrice)}₾"

                // Load image with Glide
                Glide.with(itemView.context)
                    .load(it.imageUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .into(productImage)
            } ?: run {
                productName.text = "Product not found"
                productPrice.text = "0.00₾"
            }

            quantityText.text = cartItem.quantity.toString()

            decreaseBtn.setOnClickListener {
                onCartItemChanged(cartItem, "decrease")
            }

            increaseBtn.setOnClickListener {
                onCartItemChanged(cartItem, "increase")
            }

            removeBtn.setOnClickListener {
                onCartItemChanged(cartItem, "remove")
            }
        }
    }
}