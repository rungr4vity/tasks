package il.pacolo.com.news.data.repository

import il.pacolo.com.news.data.local.dao.TaskDao
import il.pacolo.com.news.data.local.entity.toDomain
import il.pacolo.com.news.domain.model.Task
import il.pacolo.com.news.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TaskRespositoryImpl @Inject constructor(
    private val taskDao: TaskDao
): TaskRepository {
    override fun getTasks(): Flow<List<Task>> =
        taskDao.getTasks().map { entities ->

            entities.map { it ->
                it.toDomain()
            }

        }

    override suspend fun insertTask(task: Task) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteTask(task: Task) {
        TODO("Not yet implemented")
    }

    override suspend fun getTaskByDay(day: Int): Task? {
        TODO("Not yet implemented")
    }


}