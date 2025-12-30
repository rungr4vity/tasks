package il.pacolo.com.news.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import il.pacolo.com.news.data.local.entity.TaskEntity
import il.pacolo.com.news.domain.model.Task
import kotlinx.coroutines.flow.Flow


@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks")
    fun getTasks(): Flow<List<TaskEntity>>


}