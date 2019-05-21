package io.lamart.livedata.example

import androidx.lifecycle.*

class MasterViewModel(private val data: MutableLiveData<State>) : ViewModel() {

    val users: LiveData<List<String>> = data
        .map { it.users.map { it.name } }
        .distinctUntilChanged()

    fun selectUser(name: String) {
        data.value?.let { state ->
            val nextUsers = state.users
                .toMutableList()
                .map { it.copy(isSelected = it.name == name) }
            val nextState = state.copy(users = nextUsers)

            data.value = nextState
        }
    }

}
