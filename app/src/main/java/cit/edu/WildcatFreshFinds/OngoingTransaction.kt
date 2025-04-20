package cit.edu.WildcatFreshFinds

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.util.UUID
import java.util.concurrent.TimeUnit

enum class TransactionState {
    ONGOING,
    SELLER_CONFIRMED,
    BUYER_CONFIRMED,
    COMPLETED,
    CANCELLED_BY_BUYER,
    CANCELLED_BY_SELLER,
    EXPIRED
}

class TransactionStateConverter {
    @TypeConverter
    fun fromTransactionState(value: TransactionState?): String? {
        return value?.name
    }

    @TypeConverter
    fun toTransactionState(value: String?): TransactionState? {
        return value?.let { enumValueOf<TransactionState>(it) }
    }
}

@Entity(tableName = "ongoing_transactions")
@TypeConverters(TransactionStateConverter::class)
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
    var completionTimestamp: Long? = null,
    var cancellationTimestamp: Long? = null,
    val deadlineTimestamp: Long = transactionTimestamp + TimeUnit.DAYS.toMillis(3)

)
