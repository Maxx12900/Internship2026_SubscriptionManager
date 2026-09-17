package com.example.subscriptionmanager.data.dao

import androidx.room.*
import com.example.subscriptionmanager.data.entities.SubscriptionEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface SubscriptionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscription(subscription: SubscriptionEntity):Long

    @Update
    suspend fun updateSubscription(subscription: SubscriptionEntity)

    @Query ("SELECT * FROM subscriptions ORDER BY nextRenewalDate")
    fun getAllSubscriptions(): Flow<List<SubscriptionEntity>>

    @Query("SELECT * FROM subscriptions WHERE subscriptionId = :id")
    fun getSubscriptionById(id:Int) : SubscriptionEntity?

    @Query("SELECT * FROM subscriptions WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchSubscriptions(query:String): Flow<List<SubscriptionEntity>>

    @Delete
    suspend fun deleteSubscription(subscription: SubscriptionEntity)

}