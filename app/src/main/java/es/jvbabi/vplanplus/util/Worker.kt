package es.jvbabi.vplanplus.util

import android.content.Context
import androidx.work.WorkManager
import androidx.work.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

object Worker {

    fun isWorkerRunningFlow(tag: String, context: Context): Flow<Boolean> = flow {
        var oldState: Boolean? = null
        while (true) {
            val state = WorkManager.getInstance(context).getWorkInfosByTag(tag).await()
                .any { it.state == androidx.work.WorkInfo.State.RUNNING }
            if (state != oldState) {
                oldState = state
                emit(state)
            }
        }
    }

    suspend fun isWorkerRunning(tag: String, context: Context): Boolean {
        return WorkManager.getInstance(context).getWorkInfosByTag(tag).await()
            .any { it.state == androidx.work.WorkInfo.State.RUNNING }
    }
}