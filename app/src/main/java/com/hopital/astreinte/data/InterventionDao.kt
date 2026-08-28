package com.hopital.astreinte.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface InterventionDao {

    @Query("SELECT * FROM interventions ORDER BY dateMillis DESC, heureDebutMinutes DESC")
    fun getAll(): LiveData<List<Intervention>>

    @Query("SELECT * FROM interventions WHERE id = :id")
    suspend fun getById(id: Long): Intervention?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(intervention: Intervention): Long

    @Update
    suspend fun update(intervention: Intervention)

    @Query("SELECT * FROM interventions ORDER BY dateMillis, heureDebutMinutes")
    suspend fun getAllForExport(): List<Intervention>
}
