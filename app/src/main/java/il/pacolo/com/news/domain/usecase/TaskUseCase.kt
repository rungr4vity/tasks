package il.pacolo.com.news.domain.usecase

import il.pacolo.com.news.domain.model.Task
import il.pacolo.com.news.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TaskUseCase @Inject constructor(private val repository: TaskRepository) {

    operator fun invoke(): Flow<List<Task>> = repository.getTasks()

}