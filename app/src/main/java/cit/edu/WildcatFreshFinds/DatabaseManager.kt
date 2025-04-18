package cit.edu.WildcatFreshFinds

import android.content.Context

object DatabaseManager {

    fun getDatabase(context: Context): AppDatabase {
        return AppDatabase.getDatabase(context) // <-- Delegate to the correct singleton
    }

    fun productDao(context: Context): ProductDao {
        return getDatabase(context).productDao()
    }

    fun userDao(context: Context): UserDao {
        return getDatabase(context).userDao()
    }
}