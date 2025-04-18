package cit.edu.WildcatFreshFinds

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface OngoingTransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: OngoingTransaction)

    @Update
    suspend fun updateTransaction(transaction: OngoingTransaction)

    @Query("DELETE FROM ongoing_transactions WHERE transactionId = :transactionId")
    suspend fun deleteTransactionById(transactionId: String): Int // Return rows affected

    @Query("SELECT * FROM ongoing_transactions WHERE buyerEmail = :userEmail AND state = :state ORDER BY transactionTimestamp DESC")
    fun getOngoingTransactionsForBuyer(userEmail: String, state: TransactionState = TransactionState.ONGOING): LiveData<List<OngoingTransaction>>

    @Query("SELECT * FROM ongoing_transactions WHERE transactionId = :transactionId")
    suspend fun getTransactionById(transactionId: String): OngoingTransaction?


}