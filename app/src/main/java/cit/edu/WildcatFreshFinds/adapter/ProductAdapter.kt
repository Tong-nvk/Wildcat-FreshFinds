
package cit.edu.WildcatFreshFinds.adapter // Use your package name

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cit.edu.WildcatFreshFinds.R
import cit.edu.WildcatFreshFinds.data.Product
import com.bumptech.glide.Glide
import java.io.File
import java.text.NumberFormat
import java.util.Locale

class ProductAdapter : ListAdapter<Product, ProductAdapter.ProductViewHolder>(ProductDiffCallback()) {
    private var onItemClickListener: ((Product) -> Unit)? = null
    fun setOnItemClickListener(listener: (Product) -> Unit) {
        onItemClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = getItem(position)
        holder.bind(product)
    }

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.product_item_image)
        private val nameTextView: TextView = itemView.findViewById(R.id.product_item_name)
        private val priceTextView: TextView = itemView.findViewById(R.id.product_item_price)
        private val sellerTextView: TextView = itemView.findViewById(R.id.product_item_seller)

        init {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val clickedProduct = getItem(position)
                    onItemClickListener?.invoke(clickedProduct)
                }
            }
        }

        fun bind(product: Product) {
            nameTextView.text = product.name ?: "N/A"
            val format: NumberFormat = NumberFormat.getCurrencyInstance(Locale("en", "PH"))
            priceTextView.text = product.price?.let { format.format(it) } ?: "N/A"

            sellerTextView.text = " ${product.sellerEmail ?: "Unknown"}"


            Log.d("ProductAdapter", "Binding item: ${product.name}, ImageUrl is Path: ${product.imageUrl}")
            product.imageUrl?.let { filePath ->
                val imageFile = File(filePath)
                Log.d("ProductAdapter", "Checking File - Path: $filePath, Exists: ${imageFile.exists()}, CanRead: ${imageFile.canRead()}")
                if (imageFile.exists()) {
                    Log.d("ProductAdapter", "Attempting to load file path with Glide: $filePath")
                    try {
                        Glide.with(itemView.context)
                            .load(filePath)
                            .placeholder(R.drawable.empty_img)
                            .error(R.drawable.empty_img)
                            .centerCrop()
                            .into(imageView)
                    } catch (e: Exception) {
                        Log.e("ProductAdapter", "Glide loading error for path $filePath", e)
                        Glide.with(itemView.context).load(R.drawable.empty_img).centerCrop().into(imageView)
                    }
                } else {
                    Log.e("ProductAdapter", "File check FAILED for path: $filePath")
                    Glide.with(itemView.context).load(R.drawable.empty_img).centerCrop().into(imageView)
                }
            } ?: run {
                Log.w("ProductAdapter", "ImageUrl (file path) is NULL for product: ${product.name}")
                Glide.with(itemView.context).load(R.drawable.empty_img).centerCrop().into(imageView)
            }
        }
    }
}



class ProductDiffCallback : DiffUtil.ItemCallback<Product>() {
    override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
        return oldItem == newItem
    }
}