package cit.edu.WildcatFreshFinds // Or viewmodel package

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.work.WorkManager
class OngoingTransactionViewModel(application: Application) : AndroidViewModel(application) {

    private val transactionDao: OngoingTransactionDao = AppDatabase.getDatabase(application).ongoingTransactionDao()
    private val userDao: UserDao = AppDatabase.getDatabase(application).userDao()

    private val _currentUserEmail = MutableLiveData<String?>()

    val ongoingTransactions: LiveData<List<OngoingTransaction>> = _currentUserEmail.switchMap { email ->
        if (email == null) {
            MutableLiveData<List<OngoingTransaction>>()
        } else {
            transactionDao.getOngoingTransactionsForBuyer(email, TransactionState.ONGOING)
        }
    }

    private val _toastMessage = MutableLiveData<Event<String>>()
    val toastMessage: LiveData<Event<String>> = _toastMessage

    init {
        loadCurrentUserEmail()
    }

    private fun loadCurrentUserEmail() {

        viewModelScope.launch {
            val user = withContext(Dispatchers.IO) { UserManager.getSignedIn() }
            _currentUserEmail.postValue(user?.email)
        }
    }

    fun markTransactionSuccessful(transaction: OngoingTransaction) {
        Log.d("OngoingTxViewModel", "Marking successful: ${transaction.transactionId}")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                cancelTimeoutWorker(getApplication(), transaction.transactionId)
                transactionDao.deleteTransactionById(transaction.transactionId)
                _toastMessage.postValue(Event("Transaction completed successfully!"))
            } catch (e: Exception) {
                Log.e("OngoingTxViewModel", "Error marking successful: ${e.message}", e)
                _toastMessage.postValue(Event("Error completing transaction: ${e.localizedMessage}"))
            }
        }
    }

    fun markTransactionUnsuccessful(transaction: OngoingTransaction) {
        Log.d("OngoingTxViewModel", "Marking unsuccessful: ${transaction.transactionId}")
        if (transaction.sellerEmail.isBlank() || transaction.buyerEmail.isBlank()) { // Also check buyer
            Log.e("OngoingTxViewModel", "Cannot mark unsuccessful, email info missing!")
            _toastMessage.postValue(Event("Cannot mark unsuccessful: User info missing."))
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                cancelTimeoutWorker(getApplication(), transaction.transactionId)
                val sellerRows = userDao.incrementUnsuccessfulCount(transaction.sellerEmail)
                if (sellerRows > 0) Log.d("OngoingTxViewModel", "Incremented seller count: ${transaction.sellerEmail}")
                val buyerRows = userDao.incrementUnsuccessfulCount(transaction.buyerEmail)
                if (buyerRows > 0) Log.d("OngoingTxViewModel", "Incremented buyer count: ${transaction.buyerEmail}")

                transactionDao.deleteTransactionById(transaction.transactionId)
                Log.d("OngoingTxViewModel", "Deleted unsuccessful transaction.")

                _toastMessage.postValue(Event("Transaction marked unsuccessful."))

            } catch (e: Exception) {
                Log.e("OngoingTxViewModel", "Error marking unsuccessful: ${e.message}", e)
                _toastMessage.postValue(Event("Error updating transaction status: ${e.localizedMessage}"))
            }
        }
    }

    // Helper to cancel WorkManager task
    private fun cancelTimeoutWorker(appContext: Context, transactionId: String) {
        val workName = "timeout_$transactionId"
        WorkManager.getInstance(appContext).cancelUniqueWork(workName)
        Log.d("ViewModel", "Attempted to cancel timeout worker with name: $workName")
    }
}