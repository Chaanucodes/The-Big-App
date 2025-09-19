package com.faultyplay.workathome.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.faultyplay.workathome.data.local.entity.HouseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HouseDao {
    @Query("SELECT * FROM houses")
    fun observeHouses(): Flow<List<HouseEntity>>

    @Query("SELECT * FROM houses WHERE id = :houseId")
    fun observeHouse(houseId: String): Flow<HouseEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertHouse(entity: HouseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertHouses(entities: List<HouseEntity>)

    @Query("DELETE FROM houses WHERE id = :houseId")
    suspend fun deleteHouse(houseId: String)

    @Query("DELETE FROM houses")
    suspend fun clear()
}
