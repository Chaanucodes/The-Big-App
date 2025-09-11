package com.faultyplay.workathome.chore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.faultyplay.workathome.data.Chore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChoreState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ChoreViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _choreState = MutableStateFlow(ChoreState())
    val choreState = _choreState.asStateFlow()

    fun saveChore(chore: Chore, houseId: String) {
        viewModelScope.launch {
            _choreState.value = ChoreState(isLoading = true)
            val choreId = chore.id.ifEmpty { firestore.collection("houses").document(houseId).collection("chores").document().id }
            val choreToSave = chore.copy(id = choreId)

            firestore.collection("houses").document(houseId).collection("chores").document(choreId)
                .set(choreToSave)
                .addOnSuccessListener {
                    _choreState.value = ChoreState(isSuccess = true)
                }
                .addOnFailureListener { e ->
                    _choreState.value = ChoreState(error = e.message)
                }
        }
    }
}
