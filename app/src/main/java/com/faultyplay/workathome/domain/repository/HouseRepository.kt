package com.faultyplay.workathome.domain.repository

import com.faultyplay.workathome.domain.model.House
import com.faultyplay.workathome.domain.model.Member
import kotlinx.coroutines.flow.Flow

interface HouseRepository {
    fun observeHouses(userId: String): Flow<List<House>>
    fun observeHouse(houseId: String): Flow<House?>
    suspend fun refresh(userId: String)
    suspend fun createHouse(owner: Member, houseName: String, allowedContacts: List<String>): House
    suspend fun joinHouse(member: Member, inviteCode: String): Result<House>
    suspend fun updateHouse(house: House)
    suspend fun deleteHouse(houseId: String)
}
