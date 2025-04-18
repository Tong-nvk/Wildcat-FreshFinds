package cit.edu.WildcatFreshFinds

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID
import java.util.concurrent.TimeUnit

enum class TransactionState { ONGOING, SUCCESSFUL, UNSUCCESSFUL, TIMED_OUT }

@Entity(tableName = "ongoing_transactions")
data class OngoingTransaction(
    @PrimaryKey val transactionId: String = UUID.randomUUID().toString(),
    val productId: String,
    val productName: String?,
    val productImageUrl: String?,
    val pricePerItem: Double,
    val quantityBought: Int,
    val totalPrice: Double,
    val sellerEmail: String,
    val buyerEmail: String,
    val transactionTimestamp: Long = System.currentTimeMillis(),
    var state: TransactionState = TransactionState.ONGOING,
    val deadlineTimestamp: Long = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(3) // Example 3 days
)
// Note: This class doesn't necessarily need to be Parcelable unless passed via Intents