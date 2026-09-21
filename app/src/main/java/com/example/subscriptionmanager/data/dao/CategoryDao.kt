package com.example.subscriptionmanager.data.dao

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import com.example.subscriptionmanager.data.entities.Category
import com.example.subscriptionmanager.data.entities.CategoryEntity
import com.example.subscriptionmanager.data.entities.SubscriptionEntity
import kotlinx.coroutines.flow.Flow
@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)

    @Query("SELECT * FROM categories")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    // I tested it on another db, it should work :shrug:
    @Query("SELECT subscriptions.* FROM subscriptions INNER JOIN categories ON subscriptions.id = categories.subscriptionId WHERE categories.category = :category")
    fun getSubscriptionsByCategory(category: Category): Flow<List<SubscriptionEntity>>

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)

}