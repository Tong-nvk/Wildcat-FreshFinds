package cit.edu.WildcatFreshFinds

import androidx.room.Entity
import androidx.room.PrimaryKey
// If making Parcelable (Recommended for passing via Intent)
import kotlinx.parcelize.Parcelize
import android.os.Parcelable

@Parcelize // Add this annotation
@Entity(tableName = "products")
data class Product(
    @PrimaryKey val id: String,
    val name: String? = null,
    val price: Double? = null,
    val description: String? = null,
    val imageUrl: String? = null, // This holds the file path now
    val seller: String? = null,
    val quantity: Int = 0 // <-- Add quantity field (default to 0 or make non-null)
) : Parcelable // Implement Parcelable