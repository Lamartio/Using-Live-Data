package io.lamart.livedata.example

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.map

class DetailViewModel(data: LiveData<State>) : ViewModel() {

    val user: LiveData<User?> = data
        .map { state -> state.users.firstOrNull { it.isSelected } }
        .distinctUntilChanged()
    val name: LiveData<String> = user.map { it?.name ?: "" }.distinctUntilChanged()
    val age: LiveData<String> = user.map { it?.age?.toString() ?: "" }.distinctUntilChanged()

}
