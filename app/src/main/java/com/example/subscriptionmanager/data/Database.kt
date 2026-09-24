package com.example.subscriptionmanager.data

import android.content.Context
import androidx.room3.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.driver.AndroidSQLiteDriver
import com.example.subscriptionmanager.data.dao.CategoryDao
import com.example.subscriptionmanager.data.dao.NotificationDao
import com.example.subscriptionmanager.data.dao.SubscriptionDao
import com.example.subscriptionmanager.data.entities.CategoryEntity
import com.example.subscriptionmanager.data.entities.NotificationEntity
import com.example.subscriptionmanager.data.entities.SubscriptionEntity

@Database(
    entities = [
        CategoryEntity::class,
        SubscriptionEntity::class,
        NotificationEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun notificationDao(): NotificationDao
    companion object {
        @Volatile // visible to all threads
        private var INSTANCE: AppDatabase? = null // class property, isn't instance specific

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this){ // if INSTANCE not null, returns INSTANCE, otherwise it returns a new instance.
                val instance = Room.databaseBuilder<AppDatabase>(context.applicationContext, "subscriptions_db")
                    .setDriver(AndroidSQLiteDriver())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}