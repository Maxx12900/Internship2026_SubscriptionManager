package com.example.subscriptionmanager.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.subscriptionmanager.data.dao.*
import com.example.subscriptionmanager.data.entities.*

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
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "subscriptions_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}