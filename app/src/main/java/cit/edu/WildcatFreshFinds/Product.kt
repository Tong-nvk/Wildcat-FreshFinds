package cit.edu.WildcatFreshFinds

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import android.os.Parcelable

@Parcelize
@Entity(tableName = "products")
data class Product(
    @PrimaryKey val id: String,
    val name: String? = null,
    val price: Double? = null,
    val description: String? = null,
    val imageUrl: String? = null,
    val sellerEmail: String? = null,
    var quantity: Int = 0
) : Parcelable