package cit.edu.WildcatFreshFinds

import androidx.room.Entity
import androidx.room.PrimaryKey
// Import Parcelable if you were using it
// import kotlinx.parcelize.Parcelize
// import android.os.Parcelable

// @Parcelize // Keep if needed
@Entity(tableName = "users")
data class User(
    @PrimaryKey val email: String,
    // var fullName: String, // <-- REMOVE this line
    var firstName: String,  // <-- ADD this line
    var lastName: String,   // <-- ADD this line
    var password: String
) //: Parcelable // Keep if needed