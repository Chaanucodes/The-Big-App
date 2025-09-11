package com.faultyplay.workathome.house

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.faultyplay.workathome.data.House
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateHouseState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

data class JoinHouseState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

data class HouseSelectionState(
    val isLoading: Boolean = true,
    val hasHouses: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class HouseViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _houseSelectionState = MutableStateFlow(HouseSelectionState())
    val houseSelectionState = _houseSelectionState.asStateFlow()

    private val _createHouseState = MutableStateFlow(CreateHouseState())
    val createHouseState = _createHouseState.asStateFlow()

    private val _joinHouseState = MutableStateFlow(JoinHouseState())
    val joinHouseState = _joinHouseState.asStateFlow()

    init {
        checkUserHouses()
    }

    fun joinHouse(inviteCode: String) {
        viewModelScope.launch {
            _joinHouseState.value = JoinHouseState(isLoading = true)
            val user = auth.currentUser
            if (user == null) {
                _joinHouseState.value = JoinHouseState(error = "User not signed in.")
                return@launch
            }

            firestore.collection("houses").document(inviteCode).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val house = document.toObject(House::class.java)
                        if (house != null && house.invitedEmails.contains(user.email)) {
                            firestore.collection("houses").document(inviteCode)
                                .update("members", house.members + user.uid)
                                .addOnSuccessListener {
                                    _joinHouseState.value = JoinHouseState(isSuccess = true)
                                }
                                .addOnFailureListener { e ->
                                    _joinHouseState.value = JoinHouseState(error = e.message)
                                }
                        } else {
                            _joinHouseState.value = JoinHouseState(error = "You are not invited to this house.")
                        }
                    } else {
                        _joinHouseState.value = JoinHouseState(error = "Invalid invite code.")
                    }
                }
                .addOnFailureListener { e ->
                    _joinHouseState.value = JoinHouseState(error = e.message)
                }
        }
    }

    fun createHouse(houseName: String, invitedEmails: List<String>) {
        viewModelScope.launch {
            _createHouseState.value = CreateHouseState(isLoading = true)
            val user = auth.currentUser
            if (user == null) {
                _createHouseState.value = CreateHouseState(error = "User not signed in.")
                return@launch
            }

            val houseId = firestore.collection("houses").document().id
            val newHouse = House(
                id = houseId,
                name = houseName,
                members = listOf(user.uid),
                invitedEmails = invitedEmails
            )

            firestore.collection("houses").document(houseId)
                .set(newHouse)
                .addOnSuccessListener {
                    _createHouseState.value = CreateHouseState(isSuccess = true)
                }
                .addOnFailureListener { e ->
                    _createHouseState.value = CreateHouseState(error = e.message)
                }
        }
    }

    private fun checkUserHouses() {
        viewModelScope.launch {
            val user = auth.currentUser
            if (user == null) {
                _houseSelectionState.value = HouseSelectionState(isLoading = false, error = "User not signed in.")
                return@launch
            }

            firestore.collection("houses")
                .whereArrayContains("members", user.uid)
                .get()
                .addOnSuccessListener { documents ->
                    _houseSelectionState.value = HouseSelectionState(isLoading = false, hasHouses = !documents.isEmpty)
                }
                .addOnFailureListener { e ->
                    _houseSelectionState.value = HouseSelectionState(isLoading = false, error = e.message)
                }
        }
    }
}
