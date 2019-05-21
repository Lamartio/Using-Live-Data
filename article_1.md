# Using LiveData part 1: The global LiveData


What if two screens share the same data and interactivity like in master-detail flow? Then one ViewModel triggers a change and gets the update whilst the other not. This could be prevented by introducing global observable, but this will cause the repositories the have multiple responsibilities. Or the ViewModels can share a third ViewModel, but that would not make things easier.

A more elegant approach is the way frontend is doing it. There one variable is created that represents the state of the UI. This is (preferably) an immutable object and each UI part has the ability to listen to its changes. This same behavior can be created in Android by defining a single LiveData that can be accessed by any ViewModel.

Yet each ViewModel is not interested in the same part of the big UI state so lets find out how we can solve this by example. Imagine we would build a master-detail screen with in the master a list of users and in the detail we can edit the selected user his info. The required state will be:

```kotlin
data class User(
    val name: String, 
    val age: Int, 
    val isSelected: Boolean = false
)

data class State(val users: List<User> = emptyListOf())
```

This state will function as our single source of truth for the UI, meaning that there is one LiveData that is used be all the ViewModels. To make the LiveData global and accessible, we cache it in the Application class.
```kotlin

class SampleApplication : Application() {

    private val data = mutableLiveDataOf(State())
    val viewModelFactory = ViewModelFactory {
        viewModel { MasterViewModel(data) }
        viewModel { DetailViewModel(data) }
    }

}
```
There is also a `ViewModelFactory` included which is a DSL for creating a `ViewModelProvider.Factory`. If you're curious how it works; please check this [gist](https://gist.githubusercontent.com/Lamartio/c90262590a97fec2c6b39791b104f4fa/raw/2c6b42c4c39e50eb81b93782056962fa4582fd17/ViewModelFactory.kt). Within the factory we define functions that will create the ViewModels that we need. Notice that both ViewModels receive the LiveData as their first argument.

The responsibility of the `MasterViewModel` is only to show a list of available users by name. So the only LiveData required by this ViewModel is a `LiveData<List<String>>`. That means that the given LiveData needs to be mapped.

```kotlin
class MasterViewModel(data: LiveData<State>) : ViewModel() {

    val users: LiveData<List<String>> = data
        .map { it.users.map { it.name } }
        .distinctUntilChanged()

}
``` 
After mapping the data, we call `distinctUntilChanged()`, so that any observers of this data only get notified when the `List<String>` changes. For example: The customer is changing his age three times, which causes 3 state changes. But since we only showing names and not the ages, it won't affect our data. Only when a name would change, our LiveData would emit a new `List<String>`.

For the DetailViewModel we need the show the selected user and if there is no selected user, we do not show anything. A personal preference is to not write logic in the XML, so for data binding I format the fields within the ViewModel.
```kotlin
class DetailViewModel(data: LiveData<State>) : ViewModel() {

    val user: LiveData<User?> = data
        .map { state -> state.users.firstOrNull { it.isSelected } }
        .distinctUntilChanged()
    val name: LiveData<String> = user.map { it?.name ?: "" }.distinctUntilChanged()
    val age: LiveData<String> = user.map { it?.age?.toString() ?: "" }.distinctUntilChanged()

}
```
So far we only utilized a LiveData and two of its operators: `map` and `distinctUntilChanged`. We used this LiveData to setup properties that are easily bindable by either Fragment or with data binding. To make this code a working sample, we need to expand the initial state with some default users and create the Fragments. Back in the `SampleApplication` we can create a function that returns our desired demo state:
```kotlin
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
    ...
```
Our next objective is to bind the created LiveData to the UI. Explaining how the UI is set up is out of  scope of this article, so I recommend to check the [sample code](). It is a modified copy of the MasterDetail sample that is provided by Android Studio.

We have our state and UI ready, but something goes wrong. When the customer clicks on a user, it shows an empty detail screen! This happens because the `User.selected` property does not get updated. In order to do this, we need to set a click listener that triggers `MasterViewModel.selectUser`. This method should update the global state.
```kotlin
class MasterViewModel(private val data: MutableLiveData<State>) : ViewModel() {
    ...
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
```
The `selectUser` receives the name of the user, creates a new `State` and sets it back as the value of the global LiveData. Setting the value is only possible of the ViewModel has access to a MutableLiveData, so the constructor needed to be updated. 

We defined two ViewModels that prepare output based on the same LiveData. If we ever need a third ViewModel we just repeat the pattern of; creating the state, creating the ViewModel and creating the UI. 

That's all! In the next article we cover how a ViewModel can react to state changes that are happening in the Fragment. Thanks for reading! 👋

