package io.lamart.livedata.example

import android.app.Application
import io.lamart.livedata.utils.mutableLiveDataOf


private fun newDemoState() =
    State(
        users = listOf(
            User(name = "Neo", age = 42),
            User(name = "Trinity", age = 42),
            User(name = "Morpheus", age = 42)
        )
    )

class SampleApplication : Application() {

    private val data = mutableLiveDataOf(newDemoState())
    val viewModelFactory = ViewModelFactory {
        viewModel { MasterViewModel(data) }
        viewModel { DetailViewModel(data) }
    }

}

data class User(val name: String, val age: Int, val isSelected: Boolean = false)
data class State(val users: List<User> = emptyList())