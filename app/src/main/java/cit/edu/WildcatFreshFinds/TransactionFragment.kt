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

        ongoingTransactionButton = view.findViewById(R.id.ongoing_transaction_button)
        sellingItemsButton = view.findViewById(R.id.selling_items)


        view.post {
            replaceFragment(ongoingTransactionFragment)
            currentButton = ongoingTransactionButton
            setActiveButton(ongoingTransactionButton)
        }


        ongoingTransactionButton.setOnClickListener {
            Log.e("Button Click", "Ongoing Transaction Button Clicked!")
            showToast("Ongoing Transactions")

            replaceFragment(ongoingTransactionFragment)
            updateButtonState(ongoingTransactionButton)
        }



        sellingItemsButton.setOnClickListener {
            Log.e("Button Click", "Selling Items Button Clicked!")
            showToast("Selling Items")

            replaceFragment(sellingItemsFragment)
            updateButtonState(sellingItemsButton)
        }
    }

    private fun updateButtonState(newActiveButton: Button) {
        currentButton?.let { setInactiveButton(it) }
        setActiveButton(newActiveButton)
        currentButton = newActiveButton
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
        parentFragmentManager.beginTransaction().apply {
            replace(R.id.transaction_fragment_container, fragment)
            commit()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
