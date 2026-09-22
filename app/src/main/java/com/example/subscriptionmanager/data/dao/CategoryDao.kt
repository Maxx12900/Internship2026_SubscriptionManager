package com.example.subscriptionmanager.data.dao

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import com.example.subscriptionmanager.data.entities.Category
import com.example.subscriptionmanager.data.entities.CategoryEntity
import com.example.subscriptionmanager.data.entities.SubscriptionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)

    @Query("SELECT * FROM categories")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    // Let's hope this works, needs testing...
    @Query("SELECT subscriptions.* FROM subscriptions INNER JOIN categories ON subscriptions.id = categories.subscriptionId WHERE (categories.category & :category) > 0")
    fun getSubscriptionsByCategory(category: Long): Flow<List<SubscriptionEntity>>
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

@RequiresApi(Build.VERSION_CODES.O)
suspend fun sortSubscriptionsByNextRenewalDate(subscriptions: Flow<List<SubscriptionEntity>>, isAsc: Boolean): Flow<List<SubscriptionEntity>> {
    val newList = subscriptions.map {
        list -> if (isAsc) list.sortedBy { it.nextRenewalDate.toEpochMilli() } else list.sortedBy { it.nextRenewalDate.toEpochMilli() }.reversed()
    }
    return newList
}