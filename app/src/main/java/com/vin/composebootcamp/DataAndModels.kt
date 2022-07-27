package com.vin.composebootcamp

import androidx.room.*
import androidx.room.OnConflictStrategy.Companion.IGNORE
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/*
    Room DB definitions
    ========================================================
 */
const val TASK_TABLE = "TBL_TASK"

// Represents each task @ DB
@Entity(tableName = TASK_TABLE)
data class Task (
    @PrimaryKey(autoGenerate = true)
    val id: Int, // ID of entitiy
    val title: String, // title of task
    val desc: String, // description of task
    val isDone: Boolean, // is the task considered done?
    val dateAdded: Long, // date added
    val dateCompleted: Long, // date completed
)

// The stuff that accesses database
@Dao
interface TaskDAO {
    // Read
    @Query("SELECT * FROM $TASK_TABLE ORDER BY dateAdded ASC")
    fun getAllTasks(): Flow<List<Task>> // 'live' list of task entities
    @Query("SELECT * FROM $TASK_TABLE WHERE id = :id") // :id = argument 'id'
    fun getTask(id: Int): Flow<Task> // 'live' task entity
    // CUD
    @Insert(onConflict = IGNORE)
    fun addTask(task: Task)
    @Update
    fun updateTask(task: Task)
    @Delete
    fun deleteTask(task: Task)
    @Query("DELETE FROM $TASK_TABLE")
    fun clearAllTasks()
}

// Boring Room database extender
@Database(entities = [Task::class], version = 1, exportSchema = false)
abstract class TaskDB : RoomDatabase() {
    abstract fun taskDAO() : TaskDAO
}

/*
    Room DB API
    ========================================================
 */
// The real exposed interface for accessing database
interface TaskRepoInterface {
    suspend fun getAllTasks(): Flow<List<Task>>
    suspend fun getTask(id: Int): Flow<Task>
    suspend fun addTask(task: Task)
    suspend fun updateTask(task: Task)
    suspend fun deleteTask(task: Task)
    suspend fun clearAllTasks()
}
// ...and the implementation that we use
class TaskRepo (private val dao: TaskDAO) : TaskRepoInterface {
    override suspend fun getAllTasks() = dao.getAllTasks()
    override suspend fun getTask(id: Int) = dao.getTask(id)
    override suspend fun addTask(task: Task) = dao.addTask(task)
    override suspend fun updateTask(task: Task) = dao.updateTask(task)
    override suspend fun deleteTask(task: Task) = dao.deleteTask(task)
    override suspend fun clearAllTasks() = dao.clearAllTasks()
}
class TaskRepoDummy () : TaskRepoInterface {
    override suspend fun getAllTasks() = flow {
        emit(emptyList<Task>())
    }
    override suspend fun getTask(id: Int) = flow {
        emit(Task(0, "", "", false, 0, 0))
    }
    override suspend fun updateTask(task: Task) {}
    override suspend fun addTask(task: Task) {}
    override suspend fun deleteTask(task: Task) {}
    override suspend fun clearAllTasks() {}
}