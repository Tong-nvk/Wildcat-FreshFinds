package cit.edu.WildcatFreshFinds

import android.content.Context
// No need for Room import here anymore

object DatabaseManager {
    // Remove the private database instance variable if only delegating
    // private var database: AppDatabase? = null

    // Change this function to simply call the AppDatabase singleton
    fun getDatabase(context: Context): AppDatabase {
        return AppDatabase.getDatabase(context) // <-- Delegate to the correct singleton
    }

    // These functions can now stay as they are, they will use the corrected getDatabase above
    fun productDao(context: Context): ProductDao {
        return getDatabase(context).productDao()
    }

    fun userDao(context: Context): UserDao {
        return getDatabase(context).userDao()
    }
}