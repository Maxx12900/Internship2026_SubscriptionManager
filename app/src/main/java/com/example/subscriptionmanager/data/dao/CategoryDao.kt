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

    // Let's hope this works, needs testing...
    @Query("SELECT subscriptions.* FROM subscriptions INNER JOIN categories ON subscriptions.id = categories.subscriptionId WHERE categories.category & :category > 0 ORDER BY :column :order")
    fun getSubscriptionsByCategory(category: Long, column: String, order: String): Flow<List<SubscriptionEntity>>

    // Having a delete function for categories makes no sense, it is deleted automatically
}