package com.example.subscriptionmanager.data.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import com.example.subscriptionmanager.data.entities.CategoryEntity
import com.example.subscriptionmanager.data.entities.SubscriptionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)

    @Query("SELECT * FROM categories")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    // Let's hope this works, needs testing...
    @Query("SELECT subscriptions.* FROM subscriptions INNER JOIN categories ON subscriptions.id = categories.subscriptionId WHERE (categories.category & :categories) = :categories")
    fun getSubscriptionsByCategory(categories: Long): Flow<List<SubscriptionEntity>>
}

suspend fun sortSubscriptionsByPrice(subscriptions: Flow<List<SubscriptionEntity>>, isAsc: Boolean): Flow<List<SubscriptionEntity>> {
    val newList = subscriptions.map {
        list -> if (isAsc) list.sortedBy { it.price } else list.sortedBy { it.price }.reversed()
    }
    return newList
}

suspend fun sortSubscriptionsByName(subscriptions: Flow<List<SubscriptionEntity>>, isAsc: Boolean): Flow<List<SubscriptionEntity>> {
    val newList = subscriptions.map {
        list -> if (isAsc) list.sortedBy { it.name } else list.sortedBy { it.name }.reversed()
    }
    return newList
}

suspend fun sortSubscriptionsByNextRenewalDate(subscriptions: Flow<List<SubscriptionEntity>>, isAsc: Boolean): Flow<List<SubscriptionEntity>> {
    val newList = subscriptions.map {
        list -> if (isAsc) list.sortedBy { it.nextRenewalDate.timeInMillis } else list.sortedBy { it.nextRenewalDate.timeInMillis }.reversed()
    }
    return newList
}