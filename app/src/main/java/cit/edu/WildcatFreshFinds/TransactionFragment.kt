package cit.edu.WildcatFreshFinds

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

class TransactionFragment : Fragment(R.layout.fragment_transaction) {

    private lateinit var ongoingTransactionButton: Button
    private lateinit var sellingItemsButton: Button
    private var currentButton: Button? = null

    private val ongoingTransactionFragment = OngoingTransactionFragment()
    private val sellingItemsFragment = SellingItemsFragment()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("TransactionFragment", "onViewCreated called.")

        ongoingTransactionButton = view.findViewById(R.id.ongoing_transaction_button)
        sellingItemsButton = view.findViewById(R.id.selling_items)

        if (savedInstanceState == null) {
            Log.d("TransactionFragment", "savedInstanceState is null, loading default fragment.")
            replaceFragment(ongoingTransactionFragment)
            currentButton = ongoingTransactionButton
            setActiveButton(ongoingTransactionButton)
            if (::sellingItemsButton.isInitialized) {
                setInactiveButton(sellingItemsButton)
            }
        } else {
            Log.d("TransactionFragment", "savedInstanceState is NOT null, FragmentManager should restore.")
            currentButton = childFragmentManager.findFragmentById(R.id.transaction_fragment_container)?.let {
                if (it is OngoingTransactionFragment) ongoingTransactionButton else if (it is SellingItemsFragment) sellingItemsButton else null
            }
            currentButton?.let { setActiveButton(it) } // Try to restore visual state
        }


        ongoingTransactionButton.setOnClickListener {
            if (currentButton != ongoingTransactionButton) {
                Log.d("TransactionFragment", "Ongoing Transaction Button Clicked!")
                replaceFragment(ongoingTransactionFragment)
                updateButtonState(ongoingTransactionButton)
            } else {
                Log.d("TransactionFragment", "Ongoing Transaction Button Clicked (Already Active).")
            }
        }

        sellingItemsButton.setOnClickListener {
            if (currentButton != sellingItemsButton) {
                Log.d("TransactionFragment", "Selling Items Button Clicked!")
                replaceFragment(sellingItemsFragment)
                updateButtonState(sellingItemsButton)
            } else {
                Log.d("TransactionFragment", "Selling Items Button Clicked (Already Active).")
            }
        }
    }

    private fun updateButtonState(newActiveButton: Button) {
        currentButton?.let {
            if(::ongoingTransactionButton.isInitialized && ::sellingItemsButton.isInitialized) { // Check initialization
                setInactiveButton(it)
            }
        }
        setActiveButton(newActiveButton)
        currentButton = newActiveButton
        Log.d("TransactionFragment", "Updated button state, active: ${newActiveButton.id}")
    }

    private fun setActiveButton(button: Button) {
        button.setBackgroundResource(R.drawable.category_active)
        button.setTextColor(ContextCompat.getColor(requireContext(), R.color.gold))
    }

    private fun setInactiveButton(button: Button) {
        button.setBackgroundResource(R.drawable.category_inactive)
        button.setTextColor(ContextCompat.getColor(requireContext(), R.color.maroon))
    }

    private fun replaceFragment(fragment: Fragment) {
        childFragmentManager.beginTransaction().apply {
            replace(R.id.transaction_fragment_container, fragment) // Ensure this ID exists in fragment_transaction.xml
            commit()
            Log.d("TransactionFragment", "Replacing fragment with ${fragment::class.java.simpleName}")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}