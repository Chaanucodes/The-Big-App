package com.faultyplay.workathome.data.repository

import com.faultyplay.workathome.data.local.dao.HouseDao
import com.faultyplay.workathome.data.local.dao.TaskDao
import com.faultyplay.workathome.data.local.entity.HouseEntity
import com.faultyplay.workathome.data.local.entity.TaskEntity
import com.faultyplay.workathome.data.remote.FirebaseHouseService
import com.faultyplay.workathome.domain.model.House
import com.faultyplay.workathome.domain.model.Member
import com.faultyplay.workathome.domain.repository.HouseRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HouseRepositoryImpl @Inject constructor(
    private val houseDao: HouseDao,
    private val taskDao: TaskDao,
    private val remote: FirebaseHouseService,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : HouseRepository {

    override fun observeHouses(userId: String): Flow<List<House>> = combine(
        houseDao.observeHouses(),
        taskDao.observeAllTasks()
    ) { houses, tasks ->
        val groupedTasks = tasks.groupBy { it.houseId }
        houses
            .filter { entity -> entity.members.any { it.id == userId } }
            .map { entity -> entity.toDomain(groupedTasks[entity.id] ?: emptyList()) }
    }.distinctUntilChanged()

    override fun observeHouse(houseId: String): Flow<House?> = combine(
        houseDao.observeHouse(houseId),
        taskDao.observeTasks(houseId)
    ) { entity, tasks ->
        entity?.toDomain(tasks)
    }.distinctUntilChanged()

    override suspend fun refresh(userId: String) {
        withContext(dispatcher) {
            val remoteHouses = remote.fetchHousesForUser(userId)
            val houseEntities = remoteHouses.map { HouseEntity.fromDomain(it) }
            houseDao.upsertHouses(houseEntities)
            remoteHouses.forEach { house ->
                val remoteTasks = remote.fetchTasks(house.id)
                val taskEntities = remoteTasks.map { TaskEntity.fromDomain(it) }
                taskDao.upsertTasks(taskEntities)
            }
        }
    }

    override suspend fun createHouse(owner: Member, houseName: String, allowedContacts: List<String>): House =
        withContext(dispatcher) {
            val houseId = UUID.randomUUID().toString()
            val inviteCode = generateInviteCode()
            val newHouse = House(
                id = houseId,
                name = houseName,
                inviteCode = inviteCode,
                ownerId = owner.id,
                members = listOf(owner),
                allowedContacts = allowedContacts
            )
            remote.pushHouse(newHouse)
            houseDao.upsertHouse(HouseEntity.fromDomain(newHouse))
            newHouse
        }

    override suspend fun joinHouse(member: Member, inviteCode: String): Result<House> = runCatching {
        withContext(dispatcher) {
            val remoteHouse = remote.fetchHouseByInviteCode(inviteCode) ?: error("House not found")
            require(remoteHouse.canMemberJoin(member)) { "You are not allowed to join this house" }
            val members = if (remoteHouse.members.any { it.id == member.id }) {
                remoteHouse.members
            } else {
                remoteHouse.members + member
            }
            val updatedHouse = remoteHouse.copy(members = members)
            remote.pushHouse(updatedHouse)
            houseDao.upsertHouse(HouseEntity.fromDomain(updatedHouse))
            updatedHouse
        }
    }

    override suspend fun updateHouse(house: House) {
        withContext(dispatcher) {
            remote.pushHouse(house)
            houseDao.upsertHouse(HouseEntity.fromDomain(house))
        }
    }

    override suspend fun deleteHouse(houseId: String) {
        withContext(dispatcher) {
            remote.deleteHouse(houseId)
            taskDao.deleteByHouse(houseId)
            houseDao.deleteHouse(houseId)
        }
    }

    private fun generateInviteCode(): String = UUID.randomUUID().toString().take(6).uppercase()
}
