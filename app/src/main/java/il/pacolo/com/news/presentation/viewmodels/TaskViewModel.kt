package il.pacolo.com.news.presentation.viewmodels


import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope


data class User(val userId:Int,val name:String)

class TaskViewModel: ViewModel() {


    private val _data = MutableLiveData<Int>()
    private val _state = MutableStateFlow<Int>(0)
    val state: StateFlow<Int> = _state.asStateFlow()


    fun fetchUser(userId: Int, onResult: (Result<User>) -> Unit) { // this is the listener
        viewModelScope.launch {
            delay(5000)
            onResult(Result.success(User(1,"My name is Fran")))
        }
    }


    suspend fun executeTasks2() = supervisorScope {

       // but if one fails, the others continue

        val task1 = async {
            "task 1 is completed"
        }

        val task2 = async {
            "Task 2 is completed"
        }

        val results = listOf(task1,task2).awaitAll()

        results.forEach { result ->
            println(result)
        }
    }


    suspend fun executeTasks() = coroutineScope {

        val job1 = launch {

        }

        val job2 = launch {

        }

        // the coroutine scoope will wait for all coroutines to complete

    }


    fun toExecute() {
        viewModelScope.launch {
            executeTasks()
        }
    }



    fun addValue() {

        _state.value 

    }

    fun getOrders() {

        val tableOrder = viewModelScope.launch {

            launch { getHamburguers() }
            launch { getFries() }
        }



        tableOrder.cancel()
    }
    



    fun getHamburguers() {
        println("Hamburguers")
    }

    fun getFries() {
        println("fries")
    }

}