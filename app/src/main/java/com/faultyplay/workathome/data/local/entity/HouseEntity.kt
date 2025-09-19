package com.faultyplay.workathome.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.faultyplay.workathome.domain.model.House
import com.faultyplay.workathome.domain.model.Member
import com.faultyplay.workathome.domain.model.TaskSuggestion

@Entity(tableName = "houses")
data class HouseEntity(
    @PrimaryKey val id: String,
    val name: String,
    val ownerId: String,
    val inviteCode: String,
    val members: List<Member> = emptyList(),
    val allowedContacts: List<String> = emptyList(),
    val suggestions: List<TaskSuggestion> = emptyList(),
    val lastSelectedAt: Long = System.currentTimeMillis()
) {
    fun toDomain(tasks: List<TaskEntity>): House = House(
        id = id,
        name = name,
        inviteCode = inviteCode,
        ownerId = ownerId,
        members = members,
        allowedContacts = allowedContacts,
        tasks = tasks.filter { it.houseId == id }.map { it.toDomain() },
        suggestions = suggestions,
        lastSelectedAt = lastSelectedAt
    )

    companion object {
        fun fromDomain(house: House): HouseEntity = HouseEntity(
            id = house.id,
            name = house.name,
            ownerId = house.ownerId,
            inviteCode = house.inviteCode,
            members = house.members,
            allowedContacts = house.allowedContacts,
            suggestions = house.suggestions,
            lastSelectedAt = house.lastSelectedAt
        )
    }
}
