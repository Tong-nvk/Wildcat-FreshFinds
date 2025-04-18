package cit.edu.WildcatFreshFinds

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object UserManager {
    private var signedIn: User? = null
    private lateinit var userDao: UserDao // Assuming this is initialized correctly via AppDatabase now

    fun initialize(context: Context) {
        userDao = AppDatabase.getDatabase(context).userDao()
        Log.d("UserManager", "UserManager initialized with userDao: $userDao")
    }

    suspend fun registerUser(firstName: String, lastName: String, email: String, password: String): String { // <-- Changed params
        return withContext(Dispatchers.IO) {
            val existingUser = userDao.findByEmail(email)
            if (existingUser != null) {
                "Email is already taken"
            } else {
                val newUser = User(email, firstName, lastName, password) // <-- Use new params
                userDao.insert(newUser)
                "Registration Successful!"
            }
        }
    }

    suspend fun loginUser(email: String, password: String): Boolean {
        return withContext(Dispatchers.IO) {
            val user = userDao.findByEmail(email)
            if (user != null && user.password == password) {
                signedIn = user
                true
            } else {
                false
            }
        }
    }

    suspend fun editUser(firstName: String, lastName: String, password: String): Boolean { // <-- Changed params
        return withContext(Dispatchers.IO) {
            if (signedIn == null) {
                Log.w("UserManager", "editUser: No user signed in!")
                return@withContext false
            }
            val user = userDao.findByEmail(signedIn!!.email)
            if (user != null) {
                user.firstName = firstName
                user.lastName = lastName
                user.password = password
                userDao.update(user)

                signedIn = user.copy(firstName = firstName, lastName = lastName, password = password) // Use copy for immutability if User was val

                Log.d("UserManager","User edit successful for ${user.email}")
                true
            } else {
                Log.e("UserManager", "editUser: User ${signedIn!!.email} not found in database!")
                signedIn = null
                false
            }
        }
    }

    fun signOut() {
        signedIn = null
    }

    suspend fun getUsers(): List<User> {
        return withContext(Dispatchers.IO) {
            userDao.getAllUsers()
        }
    }

    suspend fun getSignedIn(): User? {
        return withContext(Dispatchers.IO) {
            val currentEmail = signedIn?.email
            if (currentEmail == null) {
                signedIn = null
                return@withContext null
            }
            val userFromDb = userDao.findByEmail(currentEmail)
            if (userFromDb == null) {
                Log.e("UserManager", "getSignedIn: User $currentEmail not found in database! Signing out.")
                signedIn = null
                return@withContext null
            }
            signedIn = userFromDb
            Log.d("UserManager", "getSignedIn: Returning user ${signedIn?.email}")
            signedIn
        }
    }
}