package cit.edu.WildcatFreshFinds // Or a 'worker' package

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TransactionTimeoutWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    companion object {
        const val KEY_TRANSACTION_ID = "TRANSACTION_ID" // Use constant for key
    }

    override suspend fun doWork(): Result {
        val transactionId = inputData.getString(KEY_TRANSACTION_ID)
            ?: run {
                Log.e("TimeoutWorker", "Missing TRANSACTION_ID in worker input data.")
                return Result.failure()
            }

        Log.d("TimeoutWorker", "Worker running for Tx: $transactionId")

        return withContext(Dispatchers.IO) { // Ensure DB access is on IO thread
            try {
                val db = AppDatabase.getDatabase(applicationContext)
                val transactionDao = db.ongoingTransactionDao()
                val userDao = db.userDao()

                val transaction = transactionDao.getTransactionById(transactionId)

                if (transaction != null && transaction.state == TransactionState.ONGOING) {
                    if (System.currentTimeMillis() >= transaction.deadlineTimestamp) {
                        Log.w("TimeoutWorker", "Transaction $transactionId TIMED OUT!")

                        val buyerEmail = transaction.buyerEmail
                        val sellerEmail = transaction.sellerEmail
                        var success = true

                        if (buyerEmail.isNotBlank()) {
                            val buyerRows = userDao.incrementUnsuccessfulCount(buyerEmail)
                            Log.d("TimeoutWorker", "Incremented buyer ($buyerEmail) count: $buyerRows rows affected.")
                        } else {
                            Log.e("TimeoutWorker", "Cannot increment buyer count, email blank for Tx $transactionId")
                            success = false
                        }

                        if (sellerEmail.isNotBlank()) {
                            val sellerRows = userDao.incrementUnsuccessfulCount(sellerEmail)
                            Log.d("TimeoutWorker", "Incremented seller ($sellerEmail) count: $sellerRows rows affected.")
                        } else {
                            Log.e("TimeoutWorker", "Cannot increment seller count, email blank for Tx $transactionId")
                            success = false
                        }

                        val deletedRows = transactionDao.deleteTransactionById(transactionId)
                        Log.d("TimeoutWorker", "Processed timeout for Tx: $transactionId. Deleted $deletedRows rows.")

                        if (success) Result.success() else Result.failure()

                    } else {
                        Log.d("TimeoutWorker", "Transaction $transactionId not yet passed deadline.")
                        Result.success()
                    }
                } else {
                    Log.d("TimeoutWorker", "Transaction $transactionId not found or no longer ONGOING.")
                    Result.success()
                }
            } catch (e: Exception) {
                Log.e("TimeoutWorker", "Error processing timeout for $transactionId", e)
                Result.retry()
            }
        }
    }
}