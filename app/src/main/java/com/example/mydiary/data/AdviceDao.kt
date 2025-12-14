package com.example.mydiary.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface AdviceDao {

    // ---------- Категории ----------
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategory(category: AdviceCategoryEntity): Long

    @Query("SELECT * FROM advice_categories WHERE name = :name LIMIT 1")
    suspend fun getCategoryByName(name: String): AdviceCategoryEntity?

    @Query("SELECT * FROM advice_categories")
    suspend fun getAllCategories(): List<AdviceCategoryEntity>

    // ---------- Советы ----------
    @Insert
    suspend fun insertAdvice(advice: AdviceEntity): Long

    @Insert
    suspend fun insertAdvices(list: List<AdviceEntity>): List<Long>

    @Update
    suspend fun updateAdvice(advice: AdviceEntity)

    @Delete
    suspend fun deleteAdvice(advice: AdviceEntity)

    @Query("SELECT * FROM advices WHERE categoryId = :categoryId")
    suspend fun getAdvicesByCategory(categoryId: Long): List<AdviceEntity>

    @Query("SELECT * FROM advices")
    suspend fun getAllAdvices(): List<AdviceEntity>
}
