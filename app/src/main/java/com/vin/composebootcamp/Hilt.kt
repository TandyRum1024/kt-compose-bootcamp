package com.vin.composebootcamp
/*
import android.app.Application
import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

// Hilt
@HiltAndroidApp
class TasksApplication : Application()
// Hilt: Inject book repo
// https://medium.com/firebase-tips-tricks/how-to-read-data-from-room-using-kotlin-flow-in-jetpack-compose-7a720dec35f5
@Module
@InstallIn(SingletonComponent::class)
class TasksAppModule {
    // Let it inject TaskDB class
    @Provides
    fun provideTaskDB(
        @ApplicationContext
        context: Context
    ) = Room.databaseBuilder(context, TaskDB::class.java, TASK_TABLE).build()
    // Same for TaskDAO class
    @Provides
    fun provideTaskDAO(taskDB: TaskDB) = taskDB.taskDAO()
    @Provides
    fun provideTaskRepoInterface(taskDAO: TaskDAO): TaskRepoInterface = TaskRepo(taskDAO)
}
*/