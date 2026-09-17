package com.example.subscriptionmanager.data.dao

import androidx.room.*
import com.example.subscriptionmanager.data.entities.CategoryEntity
import kotlinx.coroutines.flow.Flow
@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)


    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>


    @Delete
    suspend fun deleteCategory(category: CategoryEntity)

}