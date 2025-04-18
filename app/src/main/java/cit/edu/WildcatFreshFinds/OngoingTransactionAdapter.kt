package cit.edu.WildcatFreshFinds


import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.text.NumberFormat
import java.util.Locale
import androidx.core.content.ContextCompat
import java.util.concurrent.TimeUnit
class OngoingTransactionAdapter(
    private val onChatClick: (sellerEmail: String) -> Unit,
    private val onSuccessClick: (transaction: OngoingTransaction) -> Unit,
    private val onUnsuccessClick: (transaction: OngoingTransaction) -> Unit
) : ListAdapter<OngoingTransaction, OngoingTransactionAdapter.TransactionViewHolder>(TransactionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ongoing_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = getItem(position)
        holder.bind(transaction)
    }

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.transaction_item_image)
        private val nameTextView: TextView = itemView.findViewById(R.id.transaction_item_name)
        private val detailsTextView: TextView = itemView.findViewById(R.id.transaction_item_details)
        private val sellerEmailTextView: TextView = itemView.findViewById(R.id.transaction_seller_email)
        private val chatButton: Button = itemView.findViewById(R.id.button_chat_teams)
        private val successButton: Button = itemView.findViewById(R.id.button_successful)
        private val unsuccessButton: Button = itemView.findViewById(R.id.button_unsuccessful)
        private val timeRemainingTextView: TextView = itemView.findViewById(R.id.transaction_time_remaining) // <-- Find new TextView
        init {
            itemView.findViewById<Button>(R.id.button_chat_teams).setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val transaction = getItem(position)
                    onChatClick(transaction.sellerEmail)
                }
            }
            itemView.findViewById<Button>(R.id.button_successful).setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onSuccessClick(getItem(position))
                }
            }
            itemView.findViewById<Button>(R.id.button_unsuccessful).setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onUnsuccessClick(getItem(position))
                }
            }
        }

        fun bind(transaction: OngoingTransaction) {
            nameTextView.text = transaction.productName ?: "Product"
            val format: NumberFormat = NumberFormat.getCurrencyInstance(Locale("en", "PH"))
            val formattedTotal = format.format(transaction.totalPrice)
            detailsTextView.text = "Qty: ${transaction.quantityBought} | Total: $formattedTotal"
            sellerEmailTextView.text = "Seller: ${transaction.sellerEmail}"

            // Load image
            transaction.productImageUrl?.let { path ->
                Glide.with(itemView.context).load(path)
                    .placeholder(R.drawable.empty_img).error(R.drawable.empty_img)
                    .into(imageView)
            } ?: imageView.setImageResource(R.drawable.empty_img)
            val remainingTimeText = formatRemainingTime(transaction.deadlineTimestamp)
            timeRemainingTextView.text = remainingTimeText
            if (remainingTimeText.equals("Expired", ignoreCase = true)) {
                timeRemainingTextView.setTextColor(itemView.context.getColor(android.R.color.holo_red_dark)) // Example error color
            } else {
                timeRemainingTextView.setTextColor(ContextCompat.getColor(itemView.context, R.color.maroon)) // Example normal color
            }
            chatButton.setOnClickListener {
                Log.d("TransactionAdapter", "Chat clicked for seller: ${transaction.sellerEmail}")
                onChatClick(transaction.sellerEmail)
            }
            successButton.setOnClickListener {
                Log.d("TransactionAdapter", "Success clicked for tx: ${transaction.transactionId}")
                onSuccessClick(transaction)
            }
            unsuccessButton.setOnClickListener {
                Log.d("TransactionAdapter", "Unsuccessful clicked for tx: ${transaction.transactionId}")
                onUnsuccessClick(transaction)
            }
        }
    }
}
private fun formatRemainingTime(deadlineMillis: Long): String {
    val currentMillis = System.currentTimeMillis()
    val remainingMillis = deadlineMillis - currentMillis

    if (remainingMillis <= 0) {
        return "Expired"
    }

    val days = TimeUnit.MILLISECONDS.toDays(remainingMillis)
    val hours = TimeUnit.MILLISECONDS.toHours(remainingMillis) % 24
    val minutes = TimeUnit.MILLISECONDS.toMinutes(remainingMillis) % 60

    return when {
        days > 0 -> {
            val dayStr = if (days == 1L) "day" else "days"
            val hourStr = if (hours == 1L) "hour" else "hours"
            "Time Left: $days $dayStr ${hours} $hourStr"
        }

        hours > 0 -> {
            val hourStr = if (hours == 1L) "hour" else "hours"
            val minStr = if (minutes == 1L) "min" else "mins"
            "Time Left: $hours $hourStr ${minutes} $minStr"
        }

        minutes > 0 -> {
            val minStr = if (minutes == 1L) "min" else "mins"
            "Time Left: $minutes $minStr"
        }

        else -> {
            // Less than a minute left
            "Time Left: < 1 min"
        }
    }
}
class TransactionDiffCallback : DiffUtil.ItemCallback<OngoingTransaction>() {
    override fun areItemsTheSame(oldItem: OngoingTransaction, newItem: OngoingTransaction): Boolean {
        return oldItem.transactionId == newItem.transactionId
    }
    override fun areContentsTheSame(oldItem: OngoingTransaction, newItem: OngoingTransaction): Boolean {
        return oldItem == newItem
    }
}