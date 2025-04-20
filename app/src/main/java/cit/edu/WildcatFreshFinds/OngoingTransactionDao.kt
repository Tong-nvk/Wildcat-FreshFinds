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
    suspend fun deleteTransactionById(transactionId: String): Int

    @Query("SELECT * FROM ongoing_transactions WHERE buyerEmail = :userEmail AND state IN (:states) ORDER BY transactionTimestamp DESC")
    fun getBuyerTransactionsByState(userEmail: String, states: List<TransactionState>): LiveData<List<OngoingTransaction>>

    @Query("SELECT * FROM ongoing_transactions WHERE sellerEmail = :userEmail AND state IN (:states) ORDER BY transactionTimestamp DESC")
    fun getSellerTransactionsByState(userEmail: String, states: List<TransactionState>): LiveData<List<OngoingTransaction>>

    @Query("SELECT * FROM ongoing_transactions WHERE transactionId = :transactionId")
    suspend fun getTransactionById(transactionId: String): OngoingTransaction?

    @Query("UPDATE ongoing_transactions SET state = :newState, completionTimestamp = :completionTime, cancellationTimestamp = :cancellationTime WHERE transactionId = :transactionId")
    suspend fun updateTransactionState(transactionId: String, newState: TransactionState, completionTime: Long? = null, cancellationTime: Long? = null): Int

    @Query("SELECT COUNT(*) FROM ongoing_transactions WHERE productId = :productId AND state NOT IN (:finalStates)")
    suspend fun countActiveTransactionsForProduct(productId: String, finalStates: List<TransactionState> = listOf(
        TransactionState.COMPLETED,
        TransactionState.CANCELLED_BY_BUYER,
        TransactionState.CANCELLED_BY_SELLER,
        TransactionState.EXPIRED
    )): Int
}