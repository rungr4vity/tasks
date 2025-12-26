package il.pacolo.com.news.domain.repository

import il.pacolo.com.news.domain.model.Task
import kotlinx.coroutines.flow.Flow

interface TaskRepository {

    fun getTasks(): Flow<List<Task>>
    suspend fun insertTask(task: Task)
    suspend fun deleteTask(task: Task)
    suspend fun getTaskByDay(day: Int): Task?
}