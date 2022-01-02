package com.productivity.fullmangement.ui.main.task_details

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.productivity.fullmangement.R
import com.productivity.fullmangement.data.local.datastore.DataStoreManager
import com.productivity.fullmangement.data.local.datastore.SharedPrefKey
import com.productivity.fullmangement.data.local.datastore.SharedPrefValue
import com.productivity.fullmangement.data.repositories.Repository
import com.productivity.fullmangement.utils.converters.EnumDateTimeFormats
import com.productivity.fullmangement.utils.converters.asDomainModel
import com.productivity.fullmangement.utils.getResStringLanguage
import com.productivity.fullmangement.utils.showToast
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class TaskDetailsViewModel @Inject constructor(
    private val repository: Repository,
    private val args: SavedStateHandle,
    private val dataStoreManager: DataStoreManager,
    @ApplicationContext private val context: Context,
): ViewModel() {

    val taskId = args.get<Long>("taskId") ?: -1
    val task = flow {
        val task = repository
            .getTaskById(taskId)
            .asDomainModel(
                context,
                EnumDateTimeFormats.SHOW_DATE_TIME.format
            )
        emit(task)
    }

    private val _taskDetailsActions = Channel<TaskDetailsActions>()
    val taskDetailsActions = _taskDetailsActions.receiveAsFlow()

    fun submitAction(action: TaskDetailsActions) {
        viewModelScope.launch {
            _taskDetailsActions.send(action)
        }
    }

    val currLangCode = runBlocking(Dispatchers.IO) {
        dataStoreManager.readValue(
            SharedPrefKey.selectedAppLanguage,
            SharedPrefValue.DEFAULT_APP_LANGUAGE
        ).first()
    }

    fun deleteTask() = viewModelScope.launch {
        val isDeleted = repository.deleteTask(taskId)
        Timber.i("TestDelete isDeleted: $isDeleted")
        if (isDeleted > 0){
            context.showToast(context.getResStringLanguage(R.string.the_task_deleted_successfully, currLangCode))
            submitAction(TaskDetailsActions.NavigateBack)
        } else {
            context.showToast(context.getResStringLanguage(R.string.the_task_does_not_deleted_try_again_later_please, currLangCode))
        }
    }

}

sealed class TaskDetailsActions{
    object ShowSureDeleteTaskDialog: TaskDetailsActions()
    object NavigateBack: TaskDetailsActions()
    data class OpenEditTask(val taskId: Long): TaskDetailsActions()
}