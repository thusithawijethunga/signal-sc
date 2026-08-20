package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface VipMemberDao {
    @Query("SELECT * FROM vip_members ORDER BY lots DESC")
    fun getAllVipMembers(): Flow<List<VipMemberEntity>>

    @Query("SELECT * FROM vip_members WHERE period = :period ORDER BY lots DESC")
    fun getVipMembersByPeriod(period: String): Flow<List<VipMemberEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVipMember(member: VipMemberEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(members: List<VipMemberEntity>)

    @Update
    suspend fun updateVipMember(member: VipMemberEntity)

    @Query("DELETE FROM vip_members WHERE id = :id")
    suspend fun deleteVipMemberById(id: Long)

    @Query("DELETE FROM vip_members")
    suspend fun deleteAllVipMembers()

    @Query("SELECT COUNT(*) FROM vip_members")
    suspend fun getCount(): Int
}
