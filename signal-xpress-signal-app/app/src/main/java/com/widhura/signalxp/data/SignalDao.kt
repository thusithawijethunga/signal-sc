package com.widhura.signalxp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SignalDao {
    @Query("SELECT * FROM signals ORDER BY id DESC")
    fun getAllSignals(): Flow<List<SignalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSignal(signal: SignalEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(signals: List<SignalEntity>)

    @Update
    suspend fun updateSignal(signal: SignalEntity)

    @Query("DELETE FROM signals WHERE id = :id")
    suspend fun deleteSignalById(id: Long)

    @Query("SELECT * FROM signals WHERE id = :id LIMIT 1")
    suspend fun getSignalById(id: Long): SignalEntity?

    @Query("DELETE FROM signals")
    suspend fun deleteAllSignals()

    @Query("SELECT COUNT(*) FROM signals")
    suspend fun getCount(): Int
}
