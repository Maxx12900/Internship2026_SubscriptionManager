package com.example.subscriptionmanager.data.repository

import com.example.subscriptionmanager.data.dao.CategoryDao
import com.example.subscriptionmanager.data.dao.NotificationDao
import com.example.subscriptionmanager.data.dao.SubscriptionDao
import com.example.subscriptionmanager.data.dao.sortSubscriptionsByName
import com.example.subscriptionmanager.data.entities.Category
import com.example.subscriptionmanager.data.entities.CategoryEntity
import com.example.subscriptionmanager.data.entities.NotificationEntity
import com.example.subscriptionmanager.data.entities.SortBy
import com.example.subscriptionmanager.data.entities.SubscriptionEntity
import com.example.subscriptionmanager.data.entities.or
import com.example.subscriptionmanager.data.entities.toBitmask
import kotlinx.coroutines.flow.Flow

class SubscriptionRepository(
    private val subscriptionDao: SubscriptionDao,
    private val categoryDao: CategoryDao,
    private val notificationDao: NotificationDao
) {
    //Subscription operations
    fun allSubscriptions(): Flow<List<SubscriptionEntity>> =
        subscriptionDao.getAllSubscriptions()

    fun getSubscriptionById(id: Int): SubscriptionEntity? =
        subscriptionDao.getSubscriptionById(id)

    fun getSubscriptionByPackageName(packageName: String): SubscriptionEntity? =
        subscriptionDao.getSubscriptionByPackageName(packageName)

    fun searchSubscriptions(query: String): Flow<List<SubscriptionEntity>> =
        subscriptionDao.searchSubscriptions(query)

    suspend fun insertSubscription(subscription: SubscriptionEntity): Long =
        subscriptionDao.insertSubscription(subscription)

    suspend fun updateSubscription(subscription: SubscriptionEntity) =
        subscriptionDao.updateSubscription(subscription)

    suspend fun deleteSubscription(subscription: SubscriptionEntity) =
        subscriptionDao.deleteSubscription(subscription)

    // Category operations
    fun allCategories(): Flow<List<CategoryEntity>> =
        categoryDao.getAllCategories()

    suspend fun insertCategory(category: CategoryEntity)=
        categoryDao.insertCategory(category)

    // Notification operations
    fun getNotificationsForSubscription(subscriptionId: Int): Flow<List<NotificationEntity>> =
        notificationDao.getNotificationsForSubscription(subscriptionId)

    suspend fun insertNotification(notification: NotificationEntity): Long =
        notificationDao.insertNotification(notification)

    suspend fun getSubscriptions(categories: Set<Category> = emptySet(), criteria: SortBy = SortBy.NAME, isAscending: Boolean = true): Flow<List<SubscriptionEntity>> {
        val subscriptions = categoryDao.getSubscriptionsByCategory(categories.toBitmask())
        return when (criteria) {
            SortBy.NAME -> sortSubscriptionsByName(subscriptions, isAscending)
            SortBy.PRICE -> sortSubscriptionsByName(subscriptions, isAscending)
            SortBy.NEXT_RENEWAL_DATE -> sortSubscriptionsByName(subscriptions, isAscending)
        }
    }
}