package cit.edu.WildcatFreshFinds

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.text.NumberFormat
import java.util.Locale

class MyListingsAdapter(
    private val onDeleteClick: (Product) -> Unit
) : ListAdapter<Product, MyListingsAdapter.ListingViewHolder>(ProductDiffCallback()) { // Use Product DiffUtil

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_my_listings, parent, false)
        return ListingViewHolder(view)
    }

    override fun onBindViewHolder(holder: ListingViewHolder, position: Int) {
        val product = getItem(position)
        holder.bind(product)
    }

    inner class ListingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.my_listing_item_image)
        private val nameTextView: TextView = itemView.findViewById(R.id.my_listing_item_name)
        private val priceTextView: TextView = itemView.findViewById(R.id.my_listing_item_price)
        private val quantityTextView: TextView = itemView.findViewById(R.id.my_listing_item_quantity)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.button_delete_listing)

        init {
            deleteButton.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onDeleteClick(getItem(position))
                }
            }
        }

        fun bind(product: Product) {
            nameTextView.text = product.name ?: "N/A"
            quantityTextView.text = "Qty Remaining: ${product.quantity}"
            val format = NumberFormat.getCurrencyInstance(Locale("en", "PH"))
            priceTextView.text = product.price?.let { format.format(it) } ?: "N/A"

            // Load image
            product.imageUrl?.let { path ->
                Glide.with(itemView.context).load(path)
                    .placeholder(R.drawable.empty_img)
                    .error(R.drawable.empty_img)
                    .centerCrop()
                    .into(imageView)
            } ?: imageView.setImageResource(R.drawable.empty_img)


            deleteButton.isEnabled = true
            deleteButton.alpha = 1.0f
        }
    }
}

