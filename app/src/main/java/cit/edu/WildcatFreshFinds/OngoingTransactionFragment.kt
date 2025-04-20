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
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OngoingTransactionFragment : Fragment(R.layout.fragment_ongoing_transaction) {
    private val viewModel: OngoingTransactionViewModel by viewModels(
        ownerProducer = { requireParentFragment() } // Share VM with SellingItemsFragment
    )

    private lateinit var transactionRecyclerView: RecyclerView
    private lateinit var transactionAdapter: OngoingTransactionAdapter
    private lateinit var groupEmptyView: Group
    private var currentUserEmail: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ongoing_transaction, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("OngoingTransaction", "onViewCreated")
        transactionRecyclerView = view.findViewById(R.id.ongoing_transaction_recycler_view)
        groupEmptyView = view.findViewById(R.id.group_ongoing_empty)

        setupRecyclerView()
        setupObservers()

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            currentUserEmail = withContext(Dispatchers.IO) { UserManager.getSignedIn()?.email }
            Log.d("OngoingTransaction", "Current user email set to: $currentUserEmail")
            if (currentUserEmail == null) { /* Maybe disable cancel buttons? */

            }
        }
    }
    private fun setupRecyclerView() {
        Log.d("OngoingTransaction", "Setting up RecyclerView")
        transactionAdapter = OngoingTransactionAdapter(
            onChatClick = { sellerEmail -> handleChatClick(sellerEmail) },
            onConfirmClick = { transaction -> viewModel.confirmReceiptAsBuyer(transaction) }, // NEW NAME (Correct)
            onCancelClick = { transaction -> handleCancelClick(transaction) },
            onReportIssueClick = { transaction -> handleReportIssueClick(transaction) } )// Add report listener        )

        transactionRecyclerView.apply {
            adapter = transactionAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupObservers() {
        Log.d("OngoingTransaction", "Setting up ViewModel observers")
        // Observe BUYER transactions (or change query in DAO/VM if needed)
        viewModel.buyerTransactions.observe(viewLifecycleOwner, Observer { transactions ->
            Log.d("OngoingTransaction", "ViewModel BUYER transactions updated: ${transactions?.size ?: "null"} items")
            transactionAdapter.submitList(transactions ?: emptyList())
            updateEmptyView(transactions.isNullOrEmpty())
        })
        viewModel.toastMessage.observe(viewLifecycleOwner, Observer { event ->
            event.getContentIfNotHandled()?.let { message ->
                showToast(message)
            }
        })
    }

    private fun updateEmptyView(isEmpty: Boolean) {
        Log.d("OngoingTransaction", "Updating empty view visibility: isEmpty=$isEmpty")
        groupEmptyView.visibility = if (isEmpty) View.VISIBLE else View.GONE
        transactionRecyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun handleChatClick(sellerEmail: String) {
        Log.d("OngoingTransaction", "handleChatClick received sellerEmail: $sellerEmail")
        if (sellerEmail.isBlank()) { showToast("Seller email is missing."); return }
        val teamsLink = "https://teams.microsoft.com/l/chat/0/0?users=$sellerEmail"
        Log.d("OngoingTransaction", "Attempting to launch Teams link: $teamsLink")
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(teamsLink))
        try { startActivity(intent) }
        catch (e: ActivityNotFoundException) { showToast("MS Teams app or a browser is required.") }
        catch (e: Exception) { showToast("Could not start chat.") }
    }
    private fun handleCancelClick(transaction: OngoingTransaction) {
        val userEmail = currentUserEmail ?: run { showToast("Cannot cancel: User unknown."); return }

        showCustomConfirmationDialog(
            context = requireContext(),
            message = "Are you sure you want to cancel this transaction? This cannot be undone.",
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
            message = "Are you sure you want to report an issue? This will cancel the transaction and notify the seller.",
            positiveButtonText = "Report & Cancel",
            negativeButtonText = "No",
            onPositiveClick = {
                viewModel.cancelOrReportTransaction(transaction, userEmail)
            }
        )
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    fun showCustomConfirmationDialog(
        context: Context,
        message: String,
        positiveButtonText: String = "Yes",
        negativeButtonText: String = "No",
        onPositiveClick: () -> Unit,
        onNegativeClick: (() -> Unit)? = null
    ) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_confirmation)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // Make corners round

        val messageTextView = dialog.findViewById<TextView>(R.id.dialog_message_text)
        val positiveButton = dialog.findViewById<Button>(R.id.dialog_button_positive)
        val negativeButton = dialog.findViewById<Button>(R.id.dialog_button_negative)

        if (messageTextView == null || positiveButton == null || negativeButton == null) {
            Log.e("CustomDialog", "Error finding views in dialog_confirmation.xml")
            Toast.makeText(context, "Error showing dialog.", Toast.LENGTH_SHORT).show()
            return
        }

        messageTextView.text = message
        positiveButton.text = positiveButtonText
        negativeButton.text = negativeButtonText

        positiveButton.setOnClickListener {
            Log.d("CustomDialog", "Positive button clicked! Executing onPositiveClick lambda.") // <-- Add Log

            onPositiveClick()
            dialog.dismiss()
        }

        negativeButton.setOnClickListener {
            onNegativeClick?.invoke() // Execute the negative action (if provided)
            dialog.dismiss()
        }

        dialog.show()
    }
}