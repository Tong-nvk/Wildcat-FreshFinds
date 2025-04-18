package cit.edu.WildcatFreshFinds

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): LiveData<List<Product>>

    @Query("SELECT * FROM products WHERE name LIKE :searchQuery OR description LIKE :searchQuery ORDER BY name ASC")
    fun searchProducts(searchQuery: String): LiveData<List<Product>>


    @Query("SELECT id FROM products LIMIT 1")
    suspend fun getAnySingleProductId(): String?

    @Query("DELETE FROM products WHERE id = :productId")
    suspend fun deleteProductById(productId: String): Int

    @Query("DELETE FROM products")
    suspend fun deleteAllProducts()

    @Query("SELECT * FROM products WHERE id = :id")
    fun findById(id: String): Product?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: Product)

    @Update
    suspend fun update(product: Product)

    @Delete
    suspend fun delete(product: Product)
    // --- End Keep ---
}