package com.example.subscriptionmanager.data.repository

import com.example.subscriptionmanager.data.dao.CategoryDao
import com.example.subscriptionmanager.data.dao.NotificationDao
import com.example.subscriptionmanager.data.dao.SubscriptionDao
import com.example.subscriptionmanager.data.entities.CategoryEntity
import com.example.subscriptionmanager.data.entities.NotificationEntity
import com.example.subscriptionmanager.data.entities.SubscriptionEntity
import kotlinx.coroutines.flow.Flow

class SubscriptionRepository(
    private val subscriptionDao: SubscriptionDao,
    private val categoryDao: CategoryDao,
    private val notificationDao: NotificationDao
) {
    //Subscription operations
    val allSubscriptions: Flow<List<SubscriptionEntity>> =
        subscriptionDao.getAllSubscriptions()

    suspend fun getSubscriptionById(id: Int): SubscriptionEntity? =
        subscriptionDao.getSubscriptionById(id)

    fun searchSubscriptions(query: String): Flow<List<SubscriptionEntity>> =
        subscriptionDao.searchSubscriptions(query)

    suspend fun insertSubscription(subscription: SubscriptionEntity): Long =
        subscriptionDao.insertSubscription(subscription)

    suspend fun updateSubscription(subscription: SubscriptionEntity) =
        subscriptionDao.updateSubscription(subscription)

    suspend fun deleteSubscription(subscription: SubscriptionEntity) =
        subscriptionDao.deleteSubscription(subscription)

    // Category operations
    val allCategories: Flow<List<CategoryEntity>> =
        categoryDao.getAllCategories()

    suspend fun insertCategory(category: CategoryEntity)=
        categoryDao.insertCategory(category)

    // Notification operations
    fun getNotificationsForSubscription(subscriptionId: Long): Flow<List<NotificationEntity>> =
        notificationDao.getNotificationsForSubscription(subscriptionId)

    suspend fun insertNotification(notification: NotificationEntity): Long =
        notificationDao.insertNotification(notification)
}