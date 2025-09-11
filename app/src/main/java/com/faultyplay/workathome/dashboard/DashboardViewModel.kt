package com.faultyplay.workathome.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.faultyplay.workathome.data.Chore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

enum class SortType {
    URGENCY, DEADLINE
}

data class DashboardState(
    val isLoading: Boolean = true,
    val chores: List<Chore> = emptyList(),
    val error: String? = null,
    val sortType: SortType = SortType.URGENCY,
    val houseId: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state = _state.asStateFlow()

    init {
        loadChores()
    }

    private fun loadChores() {
        viewModelScope.launch {
            val user = auth.currentUser
            if (user == null) {
                _state.value = DashboardState(isLoading = false, error = "User not signed in.")
                return@launch
            }

            // For MVP, assume the user has one house. Fetch the first one found.
            firestore.collection("houses")
                .whereArrayContains("members", user.uid)
                .limit(1)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        _state.value = DashboardState(isLoading = false, error = "No house found for user.")
                        return@addOnSuccessListener
                    }
                    val houseId = documents.first().id
                    _state.update { it.copy(houseId = houseId) }
                    loadChoresForHouse(houseId)
                }
                .addOnFailureListener { e ->
                    _state.value = DashboardState(isLoading = false, error = e.message)
                }
        }
    }

    private fun loadChoresForHouse(houseId: String) {
        firestore.collection("houses").document(houseId).collection("chores")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    _state.value = DashboardState(isLoading = false, error = e.message)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val chores = snapshot.toObjects(Chore::class.java)
                    _state.update { it.copy(isLoading = false, chores = sortChores(chores, it.sortType)) }
                }
            }
    }

    fun changeSortType(sortType: SortType) {
        _state.update { it.copy(sortType = sortType, chores = sortChores(it.chores, sortType)) }
    }

    private fun sortChores(chores: List<Chore>, sortType: SortType): List<Chore> {
        return when (sortType) {
            SortType.URGENCY -> chores.sortedByDescending { it.urgency }
            SortType.DEADLINE -> chores.sortedBy { it.deadline }
        }
    }

    fun completeChore(chore: Chore, houseId: String) {
        val calendar = Calendar.getInstance()
        calendar.time = chore.deadline ?: Date()

        when (chore.recurrenceInterval) {
            "Weekly" -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
            "Monthly" -> calendar.add(Calendar.MONTH, 1)
            "Quarterly" -> calendar.add(Calendar.MONTH, 3)
            else -> return // Or handle custom intervals
        }
        val nextDeadline = calendar.time

        val updatedChore = chore.copy(
            deadline = nextDeadline,
            lastCompletedDate = Date()
        )

        firestore.collection("houses").document(houseId).collection("chores").document(chore.id)
            .set(updatedChore)
    }
}
