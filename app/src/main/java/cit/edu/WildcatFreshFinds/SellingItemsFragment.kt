package cit.edu.WildcatFreshFinds

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.Group
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels // Correct import
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SellingItemsFragment : Fragment(R.layout.fragment_selling_items) { // Use correct layout

    // Share ViewModel with parent TransactionFragment and sibling OngoingTransactionFragment
    private val viewModel: OngoingTransactionViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )

    private lateinit var sellingRecyclerView: RecyclerView // Use ID from layout
    private lateinit var sellingAdapter: SellingItemsAdapter
    private lateinit var groupEmptySellingView: Group // Use ID from layout
    private var currentUserEmail: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        return inflater.inflate(R.layout.fragment_selling_items, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("SellingItemsFragment", "onViewCreated")

        // Find views using IDs from fragment_selling_items.xml
        sellingRecyclerView = view.findViewById(R.id.selling_items_recycler_view)
        groupEmptySellingView = view.findViewById(R.id.group_selling_empty)

        setupRecyclerView()
        setupObservers()

        // Fetch current user email (needed for cancelling/reporting)
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            currentUserEmail = withContext(Dispatchers.IO) { UserManager.getSignedIn()?.email }
            Log.d("SellingItemsFragment", "Current user email set to: $currentUserEmail")
        }
    }

    private fun setupRecyclerView() {
        Log.d("SellingItemsFragment", "Setting up RecyclerView")
        sellingAdapter = SellingItemsAdapter( // Instantiate SellingItemsAdapter
            // Map listeners to handlers or ViewModel methods
            onChatBuyerClick = { buyerEmail -> handleChatClick(buyerEmail) },
            onConfirmHandoverClick = { transaction -> viewModel.confirmHandoverAsSeller(transaction) }, // Seller confirms
            onCancelClick = { transaction -> handleCancelClick(transaction) },
            onReportIssueClick = { transaction -> handleReportIssueClick(transaction) }
        )
        sellingRecyclerView.apply {
            adapter = sellingAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupObservers() {
        Log.d("SellingItemsFragment", "Setting up ViewModel observers")
        // ---> Observe SELLER transactions <---
        viewModel.sellerTransactions.observe(viewLifecycleOwner, Observer { transactions ->
            Log.d("SellingItemsFragment", "ViewModel SELLER transactions updated: ${transactions?.size ?: "null"} items")
            sellingAdapter.submitList(transactions ?: emptyList())
            updateEmptyView(transactions.isNullOrEmpty())
        })

        // Observe shared toast messages
        viewModel.toastMessage.observe(viewLifecycleOwner, Observer { event ->
            event.getContentIfNotHandled()?.let { message ->
                showToast(message)
            }
        })
    }

    private fun updateEmptyView(isEmpty: Boolean) {
        Log.d("SellingItemsFragment", "Updating empty view visibility: isEmpty=$isEmpty")
        groupEmptySellingView.visibility = if (isEmpty) View.VISIBLE else View.GONE
        sellingRecyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    // Chat logic is the same, just chats with buyer now
    private fun handleChatClick(buyerEmail: String) {
        Log.d("SellingItemsFragment", "handleChatClick received buyerEmail: $buyerEmail")
        if (buyerEmail.isBlank()) { showToast("Buyer email is missing."); return }
        val teamsLink = "https://teams.microsoft.com/l/chat/0/0?users=$buyerEmail"
        Log.d("SellingItemsFragment", "Attempting to launch Teams link: $teamsLink")
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(teamsLink))
        try { startActivity(intent) }
        catch (e: ActivityNotFoundException) { showToast("MS Teams app or a browser is required.") }
        catch (e: Exception) { showToast("Could not start chat.") }
    }

    // Cancel logic is the same (ViewModel handles who cancelled)
    private fun handleCancelClick(transaction: OngoingTransaction) {
        val userEmail = currentUserEmail ?: run { showToast("Cannot cancel: User unknown."); return }
        showCustomConfirmationDialog(
            context = requireContext(),
            message = "Are you sure? This will restore product quantity and notify the buyer.",
            positiveButtonText = "Yes, Cancel",
            negativeButtonText = "No",
            onPositiveClick = {
                viewModel.cancelOrReportTransaction(transaction, userEmail)
            }
        )
    }

    private fun handleReportIssueClick(transaction: OngoingTransaction) {
        val userEmail = currentUserEmail ?: run { showToast("Cannot report: User unknown."); return }
        showCustomConfirmationDialog(
            context = requireContext(),
            message = "Are you sure you want to report an issue? This will cancel the transaction and notify the buyer.",
            positiveButtonText = "Report & Cancel",
            negativeButtonText = "No",
            onPositiveClick = {
                viewModel.cancelOrReportTransaction(transaction, userEmail) // Report uses same VM logic
            }
        )
    }
    fun showCustomConfirmationDialog(
        context: Context,
        message: String,
        positiveButtonText: String = "Yes", // Default texts
        negativeButtonText: String = "No",
        onPositiveClick: () -> Unit, // Lambda for positive action
        onNegativeClick: (() -> Unit)? = null // Optional lambda for negative action
    ) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false) // Or true if you want outside clicks to dismiss
        dialog.setContentView(R.layout.dialog_confirmation) // Use the new layout
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // Make corners round

        // Find views inside the custom dialog layout
        val messageTextView = dialog.findViewById<TextView>(R.id.dialog_message_text)
        val positiveButton = dialog.findViewById<Button>(R.id.dialog_button_positive)
        val negativeButton = dialog.findViewById<Button>(R.id.dialog_button_negative)

        // Check if views were found
        if (messageTextView == null || positiveButton == null || negativeButton == null) {
            Log.e("CustomDialog", "Error finding views in dialog_confirmation.xml")
            Toast.makeText(context, "Error showing dialog.", Toast.LENGTH_SHORT).show()
            return
        }

        // Set the content
        messageTextView.text = message
        positiveButton.text = positiveButtonText
        negativeButton.text = negativeButtonText

        // Set click listeners
        positiveButton.setOnClickListener {
            onPositiveClick() // Execute the positive action
            dialog.dismiss()
        }

        negativeButton.setOnClickListener {
            onNegativeClick?.invoke() // Execute the negative action (if provided)
            dialog.dismiss()
        }

        dialog.show()
    }
    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}