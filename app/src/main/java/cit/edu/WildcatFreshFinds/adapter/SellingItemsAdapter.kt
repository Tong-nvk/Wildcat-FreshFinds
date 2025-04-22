package cit.edu.WildcatFreshFinds.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.text.NumberFormat
import java.util.Locale
import java.util.concurrent.TimeUnit
import android.widget.ImageButton
import cit.edu.WildcatFreshFinds.R
import cit.edu.WildcatFreshFinds.data.OngoingTransaction
import cit.edu.WildcatFreshFinds.data.TransactionState


class SellingItemsAdapter(
    private val onChatBuyerClick: (buyerEmail: String) -> Unit,
    private val onConfirmHandoverClick: (transaction: OngoingTransaction) -> Unit,
    private val onCancelClick: (transaction: OngoingTransaction) -> Unit,
    private val onReportIssueClick: (transaction: OngoingTransaction) -> Unit
) : ListAdapter<OngoingTransaction, SellingItemsAdapter.SellingItemViewHolder>(
    TransactionDiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SellingItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_selling_item, parent, false)
        return SellingItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: SellingItemViewHolder, position: Int) {
        val transaction = getItem(position)
        holder.bind(transaction)
    }

    inner class SellingItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.selling_item_image)
        private val nameTextView: TextView = itemView.findViewById(R.id.selling_item_name)
        private val detailsTextView: TextView = itemView.findViewById(R.id.selling_item_details)
        private val buyerEmailTextView: TextView = itemView.findViewById(R.id.selling_buyer_email)
        private val statusTextView: TextView = itemView.findViewById(R.id.selling_transaction_status)
        private val timeRemainingTextView: TextView = itemView.findViewById(R.id.selling_time_remaining)
        private val chatButton: Button = itemView.findViewById(R.id.button_chat_buyer)
        private val confirmButton: Button = itemView.findViewById(R.id.button_confirm_handover)
        private val cancelButton: Button = itemView.findViewById(R.id.button_cancel_selling)
        private val reportButton: ImageButton = itemView.findViewById(R.id.button_report_issue_selling)

        init {
            chatButton.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) onChatBuyerClick(getItem(position).buyerEmail)
            }
            confirmButton.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) onConfirmHandoverClick(getItem(position))
            }
            cancelButton.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) onCancelClick(getItem(position))
            }
            reportButton.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) onReportIssueClick(getItem(position))
            }
        }

        fun bind(transaction: OngoingTransaction) {
            val context = itemView.context
            nameTextView.text = transaction.productName ?: "Product"
            val format: NumberFormat = NumberFormat.getCurrencyInstance(Locale("en", "PH"))
            val formattedTotal = format.format(transaction.totalPrice)
            detailsTextView.text = "Qty: ${transaction.quantityBought} | Total: $formattedTotal"
            buyerEmailTextView.text = "Buyer: ${transaction.buyerEmail}"

            transaction.productImageUrl?.let { path ->
                Glide.with(context).load(path)
                    .placeholder(R.drawable.empty_img).error(R.drawable.empty_img).centerCrop()
                    .into(imageView)
            } ?: imageView.setImageResource(R.drawable.empty_img)

            val remainingTimeText = formatRemainingTime(transaction.deadlineTimestamp)
            timeRemainingTextView.text = remainingTimeText
            timeRemainingTextView.setTextColor(ContextCompat.getColor(context,
                if (remainingTimeText == "Expired") android.R.color.holo_red_dark else R.color.maroon // Use your color
            ))

            var statusText = ""
            var showConfirm = false
            var showCancel = false
            var showReport = false
            var showChat = false

            when (transaction.state) {
                TransactionState.ONGOING -> {
                    statusText = "Status: Waiting for confirmations"
                    showConfirm = true
                    showCancel = true
                    showReport = true
                    showChat = true
                }
                TransactionState.BUYER_CONFIRMED -> {
                    statusText = "Status: Buyer confirmed receipt"
                    showConfirm = true
                    showCancel = false
                    showReport = true
                    showChat = true
                }
                TransactionState.SELLER_CONFIRMED -> {
                    statusText = "Status: You confirmed handover"
                    showConfirm = false
                    showCancel = false
                    showReport = false
                    showChat = true
                }
                TransactionState.COMPLETED -> {
                    statusText = "Status: Completed ${formatTimestamp(transaction.completionTimestamp)}"
                }
                TransactionState.CANCELLED_BY_BUYER -> {
                    statusText = "Status: Cancelled by Buyer ${formatTimestamp(transaction.cancellationTimestamp)}"
                }
                TransactionState.CANCELLED_BY_SELLER -> {
                    statusText = "Status: Cancelled by You ${formatTimestamp(transaction.cancellationTimestamp)}"
                }
                TransactionState.EXPIRED -> {
                    statusText = "Status: Expired"
                }
            }

            statusTextView.text = statusText
            confirmButton.visibility = if(showConfirm) View.VISIBLE else View.GONE
            confirmButton.text = "Confirm Handover" // Correct text for seller
            cancelButton.visibility = if(showCancel) View.VISIBLE else View.GONE
            cancelButton.text = "Cancel"
            reportButton.visibility = if(showReport) View.VISIBLE else View.GONE
            chatButton.visibility = if(showChat) View.VISIBLE else View.GONE
            chatButton.text = "Message Buyer" // Or just Message
        }
    }

    // Helper Function to Format Time (Copied from OngoingTransactionAdapter)
    private fun formatRemainingTime(deadlineMillis: Long): String {
        val currentMillis = System.currentTimeMillis()
        val remainingMillis = deadlineMillis - currentMillis
        if (remainingMillis <= 0) { return "Expired" }
        val days = TimeUnit.MILLISECONDS.toDays(remainingMillis)
        val hours = TimeUnit.MILLISECONDS.toHours(remainingMillis) % 24
        return when {
            days >= 2 -> "Time Left: $days days"
            days == 1L -> "Time Left: $days day ${hours}h"
            hours > 0 -> "Time Left: $hours hours"
            else -> "Time Left: < ${hours+1} hours"
        }
    }

    // Helper to format timestamp (Copied from OngoingTransactionAdapter)
    private fun formatTimestamp(timestamp: Long?): String {
        return timestamp?.let {
            android.text.format.DateFormat.format("MMM dd, yyyy", it).toString()
        } ?: ""
    }

}