package cit.edu.WildcatFreshFinds

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.Group
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import androidx.fragment.app.viewModels

class OngoingTransactionFragment : Fragment(R.layout.fragment_ongoing_transaction) {

    private val viewModel: OngoingTransactionViewModel by viewModels()

    private lateinit var transactionRecyclerView: RecyclerView
    private lateinit var transactionAdapter: OngoingTransactionAdapter
    private lateinit var groupEmptyView: Group

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
    }

    private fun setupRecyclerView() {
        Log.d("OngoingTransaction", "Setting up RecyclerView")
        transactionAdapter = OngoingTransactionAdapter(
            onChatClick = { sellerEmail -> handleChatClick(sellerEmail) },
            onSuccessClick = { transaction -> viewModel.markTransactionSuccessful(transaction) },
            onUnsuccessClick = { transaction -> viewModel.markTransactionUnsuccessful(transaction) }
        )
        transactionRecyclerView.apply {
            adapter = transactionAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupObservers() {
        Log.d("OngoingTransaction", "Setting up ViewModel observers")
        // Observe the list of transactions from the ViewModel
        // This LiveData is driven by the user email loading within the ViewModel
        viewModel.ongoingTransactions.observe(viewLifecycleOwner, Observer { transactions ->
            Log.d("OngoingTransaction", "ViewModel transactions LiveData updated: ${transactions?.size ?: "null"} items")
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


    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}