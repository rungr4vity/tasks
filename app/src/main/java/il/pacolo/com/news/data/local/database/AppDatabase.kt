package il.pacolo.com.news.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import il.pacolo.com.news.data.local.dao.TaskDao
import il.pacolo.com.news.data.local.entity.TaskEntity


@Database(
    entities = [TaskEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase: RoomDatabase() {
    abstract fun taskDao(): TaskDao
}