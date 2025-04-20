package cit.edu.WildcatFreshFinds

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class TransactionTimeoutWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    companion object {
        const val KEY_TRANSACTION_ID = "TRANSACTION_ID"
    }

    override suspend fun doWork(): Result {
        val transactionId = inputData.getString(KEY_TRANSACTION_ID)
            ?: run {
                Log.e("TimeoutWorker", "Missing TRANSACTION_ID in worker input data.")
                return Result.failure()
            }

        Log.d("TimeoutWorker", "Worker running for Tx: $transactionId")

        return withContext(Dispatchers.IO) {
            try {
                val db = AppDatabase.getDatabase(applicationContext)
                val transactionDao = db.ongoingTransactionDao()
                val productDao = db.productDao()

                val transaction = transactionDao.getTransactionById(transactionId)

                val expirableStates = listOf(
                    TransactionState.ONGOING,
                    TransactionState.BUYER_CONFIRMED,
                    TransactionState.SELLER_CONFIRMED
                )

                if (transaction != null && transaction.state in expirableStates) {
                    // Check if deadline timestamp has passed
                    if (System.currentTimeMillis() >= transaction.deadlineTimestamp) {
                        Log.w("TimeoutWorker", "Transaction $transactionId TIMED OUT!")

                        val qtyRestored = productDao.addQuantityToProduct(transaction.productId, transaction.quantityBought)
                        if(qtyRestored > 0) Log.d("TimeoutWorker", "Restored quantity for product: ${transaction.productId}")
                        else Log.w("TimeoutWorker", "Failed to restore quantity for product: ${transaction.productId}")


                        val updatedRows = transactionDao.updateTransactionState(transactionId, TransactionState.EXPIRED)
                        Log.d("TimeoutWorker", "Marked transaction $transactionId as EXPIRED ($updatedRows rows).")


                        Result.success()

                    } else {
                        Log.d("TimeoutWorker", "Transaction $transactionId has not timed out yet (Deadline: ${transaction.deadlineTimestamp}).")
                        Result.success()
                    }
                } else {
                    Log.d("TimeoutWorker", "Transaction $transactionId not found or not in an expirable state (${transaction?.state}). Work is done.")
                    Result.success()
                }
            } catch (e: Exception) {
                Log.e("TimeoutWorker", "Error processing timeout for $transactionId", e)
                Result.retry()
            }
        }
    }
}