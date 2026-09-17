package com.example.subscriptionmanager.data.entities

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "Categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val categoryId: Int = 0,
    val name:String,
    val iconName:String? = null

)
