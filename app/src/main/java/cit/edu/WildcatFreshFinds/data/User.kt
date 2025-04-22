package cit.edu.WildcatFreshFinds.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import android.os.Parcelable

@Parcelize
@Entity(tableName = "users")
data class User(
    @PrimaryKey val email: String,
    var firstName: String,
    var lastName: String,
    var password: String,
    var receivedCancellationCount: Int = 0,
    var profileImagePath: String? = null
) : Parcelable