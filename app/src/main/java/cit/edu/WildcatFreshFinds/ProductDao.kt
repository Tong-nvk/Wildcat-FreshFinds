package cit.edu.WildcatFreshFinds

import androidx.lifecycle.LiveData // <-- Import LiveData
import androidx.room.*

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY name ASC") // Example ordering
    fun getAllProducts(): LiveData<List<Product>> // <-- Return LiveData

    // Keep other methods as they are (findById, insert, update, delete)
    @Query("SELECT * FROM products WHERE id = :id")
    fun findById(id: String): Product? // This can stay synchronous if needed

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: Product)

    @Update
    suspend fun update(product: Product) // Recommend suspend

    @Delete
    suspend fun delete(product: Product) // Recommend suspend

    // --- Add this method ---
    @Query("DELETE FROM products") // SQL to delete all rows
    suspend fun deleteAllProducts()
}