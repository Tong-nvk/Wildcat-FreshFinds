package cit.edu.WildcatFreshFinds

import android.content.Context
import androidx.room.* // Import required Room components
// Import all DAOs and Entities
import cit.edu.WildcatFreshFinds.UserDao
import cit.edu.WildcatFreshFinds.ProductDao
import cit.edu.WildcatFreshFinds.OngoingTransactionDao
import cit.edu.WildcatFreshFinds.User
import cit.edu.WildcatFreshFinds.Product
import cit.edu.WildcatFreshFinds.OngoingTransaction
import cit.edu.WildcatFreshFinds.TransactionStateConverter // Import converter


@Database(
    entities = [User::class, Product::class, OngoingTransaction::class],
    version = 7,
    exportSchema = false
)
@TypeConverters(TransactionStateConverter::class) // <-- Add TypeConverter for Enum
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun productDao(): ProductDao
    abstract fun ongoingTransactionDao(): OngoingTransactionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "wildcat_fresh_finds_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}