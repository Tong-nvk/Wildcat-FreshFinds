package cit.edu.WildcatFreshFinds

import androidx.room.*

@Dao
interface UserDao {
    @Query("SELECT * FROM users")
    fun getAllUsers(): List<User>

    @Query("SELECT * FROM users WHERE email = :email")
    fun findByEmail(email: String): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(user: User)

    @Update
    fun update(user: User)

    @Delete
    fun delete(user: User)


    @Query("UPDATE users SET receivedCancellationCount = receivedCancellationCount + 1 WHERE email = :email")
    suspend fun incrementReceivedCancellations(email: String): Int


}