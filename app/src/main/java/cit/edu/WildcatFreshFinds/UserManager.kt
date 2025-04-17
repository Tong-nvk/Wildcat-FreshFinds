package cit.edu.WildcatFreshFinds

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object UserManager {
    private var signedIn: User? = null
    private lateinit var userDao: UserDao // Assuming this is initialized correctly via AppDatabase now

    // Keep initialize function, ensure it uses AppDatabase.getDatabase(...)
    fun initialize(context: Context) {
        // Make sure this uses the correct singleton access
        userDao = AppDatabase.getDatabase(context).userDao()
        Log.d("UserManager", "UserManager initialized with userDao: $userDao")
    }

    // Modify registerUser signature and User creation
    suspend fun registerUser(firstName: String, lastName: String, email: String, password: String): String { // <-- Changed params
        return withContext(Dispatchers.IO) {
            val existingUser = userDao.findByEmail(email)
            if (existingUser != null) {
                "Email is already taken"
            } else {
                // Use new fields when creating User
                val newUser = User(email, firstName, lastName, password) // <-- Use new params
                userDao.insert(newUser)
                "Registration Successful!"
            }
        }
    }

    // loginUser doesn't need internal changes, still works with email/password
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

    // Modify editUser signature and update logic
    suspend fun editUser(firstName: String, lastName: String, password: String): Boolean { // <-- Changed params
        return withContext(Dispatchers.IO) {
            if (signedIn == null) {
                Log.w("UserManager", "editUser: No user signed in!")
                return@withContext false
            }
            // Fetch the user from DB to ensure we have the latest before editing
            val user = userDao.findByEmail(signedIn!!.email)
            if (user != null) {
                // Update new fields
                user.firstName = firstName // <-- Update firstName
                user.lastName = lastName   // <-- Update lastName
                user.password = password
                userDao.update(user) // Save changes to DB

                // Update the cached signedIn user object as well
                signedIn = user.copy(firstName = firstName, lastName = lastName, password = password) // Use copy for immutability if User was val

                Log.d("UserManager","User edit successful for ${user.email}")
                true
            } else {
                Log.e("UserManager", "editUser: User ${signedIn!!.email} not found in database!")
                // Sign out if user magically disappeared from DB?
                signedIn = null
                false
            }
        }
    }

    // signOut doesn't need changes
    fun signOut() {
        signedIn = null
    }

    // getUsers doesn't need changes
    suspend fun getUsers(): List<User> {
        return withContext(Dispatchers.IO) {
            userDao.getAllUsers()
        }
    }

    // getSignedIn needs to re-fetch from DB to ensure data consistency
    // The returned User object will have firstName/lastName
    suspend fun getSignedIn(): User? {
        return withContext(Dispatchers.IO) {
            val currentEmail = signedIn?.email
            if (currentEmail == null) {
                // Log.w("UserManager", "getSignedIn: No user email cached in signedIn!") // Optional log
                signedIn = null // Ensure signedIn is null if email is missing
                return@withContext null
            }
            // Always fetch fresh data from DB
            val userFromDb = userDao.findByEmail(currentEmail)
            if (userFromDb == null) {
                Log.e("UserManager", "getSignedIn: User $currentEmail not found in database! Signing out.")
                signedIn = null // Sign out if not found in DB
                return@withContext null
            }
            // Update the cached signedIn user
            signedIn = userFromDb
            Log.d("UserManager", "getSignedIn: Returning user ${signedIn?.email}")
            signedIn // Return the fresh user data
        }
    }
}