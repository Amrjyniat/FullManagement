package com.productivity.fullmangement.ui.main.tasks_list

import android.content.Context
import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.productivity.fullmangement.R
import com.productivity.fullmangement.data.domain.FiltersData
import com.productivity.fullmangement.data.domain.TaskDomain
import com.productivity.fullmangement.data.local.TaskType
import com.productivity.fullmangement.data.local.datastore.DataStoreManager
import com.productivity.fullmangement.data.local.datastore.SharedPrefKey
import com.productivity.fullmangement.data.local.datastore.SharedPrefValue
import com.productivity.fullmangement.data.repositories.Repository
import com.productivity.fullmangement.utils.converters.EnumDateTimeFormats
import com.productivity.fullmangement.utils.converters.asDomainModel
import com.productivity.fullmangement.utils.getResStringLanguage
import com.productivity.fullmangement.utils.showToast
import com.productivity.fullmangement.utils.tasksForPreview
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import timber.log.Timber
import javax.inject.Inject


@HiltViewModel
class TasksViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dataStoreManager: DataStoreManager,
    private val repository: Repository,
    private val args: SavedStateHandle
): ViewModel() {

    var taskType = args.get<TaskType>("tasksType") ?: TaskType.ALL

    val currLangCode = runBlocking(Dispatchers.IO) {
        dataStoreManager.readValue(
            SharedPrefKey.selectedAppLanguage,
            SharedPrefValue.DEFAULT_APP_LANGUAGE
        ).first()
    }

    private val query = MutableStateFlow("")

    private val filters = MutableStateFlow(FiltersData())
    fun applyFilters(filters: FiltersData){
        this.filters.value = filters
    }
    fun clearFilters(){
        this.filters.value = FiltersData()
    }

    private val filtersToApply = MutableStateFlow(FiltersData())
    fun showResults(){
        filtersToApply.value = filters.value
    }
    fun clearAppliedFilters(){
        filtersToApply.value = FiltersData()
    }

    @ExperimentalCoroutinesApi
    var tasksList = combine(query, filtersToApply) { query, filtersToApply ->
        //set ui filters to be equal applied filters whenever the applied changed
        applyFilters(filtersToApply)
        Timber.i("TestFilter flatMapLatest filtersToApply: ${filtersToApply} , query: $query")
        repository.getTasks(taskType, query, filtersToApply).map {
            it.asDomainModel(context, EnumDateTimeFormats.SHOW_DATE_TIME.format)
        }
    }.flatMapLatest { it }.distinctUntilChanged()
    fun onQueryChanged(newText: String) {
        query.value = newText
    }

    fun refreshData() = viewModelScope.launch {
        delay(500)
        tasksList = repository.getTasks(taskType, query.value, filters.value).map {
            it.asDomainModel(
                context, EnumDateTimeFormats.SHOW_DATE_TIME.format
            )
        }
    }

    private val _tasksActions = Channel<TasksActions>()
    val tasksActions = _tasksActions.receiveAsFlow()

    fun submitAction(action: TasksActions) {
        viewModelScope.launch {
            _tasksActions.send(action)
        }
    }

    fun archiveTask(taskId: Long) = viewModelScope.launch {
        val isArchivedSuccessfully = repository.archiveTask(taskId)
        onItemCollapsed(taskId.toInt())
        if (isArchivedSuccessfully == 1) {
            context.showToast(context.getResStringLanguage(R.string.archived_successfully, currLangCode))
            refreshData()
        } else {
            context.showToast(context.getResStringLanguage(R.string.archived_failed_try_again_later_please, currLangCode))
        }
    }

    fun unarchiveTask(taskId: Long) = viewModelScope.launch {
        val isUnarchivedSuccessfully = repository.unarchiveTask(taskId)
        onItemCollapsed(taskId.toInt())
        if (isUnarchivedSuccessfully == 1) {
            context.showToast(context.getResStringLanguage(R.string.unarchived_successfully, currLangCode))
            refreshData()
        } else {
            context.showToast(context.getResStringLanguage(R.string.unarchived_failed_try_again_later_please, currLangCode))
        }
    }

    private val _revealedTasksIds = MutableStateFlow(listOf<Int>())
    val revealedTaskIdsList: StateFlow<List<Int>> get() = _revealedTasksIds

    //Swipe the card show the options and add the member id to the list
    fun onItemExpanded(userId: Int) {
        if (_revealedTasksIds.value.contains(userId)) return
        _revealedTasksIds.value = _revealedTasksIds.value.toMutableList().also { list ->
            list.add(userId)
        }
    }

    //Return the card to its normal state and remove the member id from the list
    fun onItemCollapsed(userId: Int) {
        if (!_revealedTasksIds.value.contains(userId)) return
        _revealedTasksIds.value = _revealedTasksIds.value.toMutableList().also { list ->
            list.remove(userId)
        }
    }

    fun deleteTask(taskId: Long) = viewModelScope.launch {
        repository.deleteTask(taskId)
    }

    val tasksUiState = combine(revealedTaskIdsList, tasksList, filters, filtersToApply, query){ _, list, filters, filtersToApply, query ->
        TasksUiState(
            tasksType = taskType,
            revealTasksId = revealedTaskIdsList,
            tasksList = list,
            query = query,
            filters = filters,
            filtersToApply = filtersToApply,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), TasksUiState.Empty)

}

@Immutable
data class TasksUiState(
    val tasksType: TaskType = TaskType.ALL,
    val revealTasksId: Flow<List<Int>> = MutableStateFlow(listOf()),
    val tasksList: List<TaskDomain> = listOf(),
    val query: String = "",
    val filters: FiltersData = FiltersData(),
    val filtersToApply: FiltersData = FiltersData(),
){
    companion object {
        val Empty = TasksUiState()
        val ContainTasks = TasksUiState(tasksList = tasksForPreview)
    }
}

sealed class TasksActions{
    object OpenAddTaskAction: TasksActions()
    data class  ShowSureDeleteTaskDialog(val taskId: Long): TasksActions()
    data class OpenTaskDetails(val taskId: Long): TasksActions()
}