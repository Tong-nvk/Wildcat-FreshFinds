package cit.edu.WildcatFreshFinds.dao

import android.content.Context
import androidx.room.* // Import required Room components
import cit.edu.WildcatFreshFinds.data.OngoingTransaction
import cit.edu.WildcatFreshFinds.data.Product
import cit.edu.WildcatFreshFinds.data.TransactionStateConverter
import cit.edu.WildcatFreshFinds.data.User
// Import all DAOs and Entities


@Database(
    entities = [User::class, Product::class, OngoingTransaction::class],
    version = 8,
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