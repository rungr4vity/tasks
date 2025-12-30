package il.pacolo.com.news.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import il.pacolo.com.news.domain.model.Task


@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,val name: String,val description: String,val url: String,val day: Int
)


fun TaskEntity.toDomain() = Task(
    id = id,
    name = name,
    description = description,
    url = url,
    day = day
)

fun TaskEntity.toEntity() = TaskEntity(
    id = id,
    name = name,
    description = description,
    url = url,
    day = day
)