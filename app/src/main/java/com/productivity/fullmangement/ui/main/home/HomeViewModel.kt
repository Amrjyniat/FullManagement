package com.productivity.fullmangement.ui.main.home

import android.content.Context
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.collectAsState
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.productivity.fullmangement.data.domain.TaskDomain
import com.productivity.fullmangement.data.local.DataBaseQueries
import com.productivity.fullmangement.data.local.TaskType
import com.productivity.fullmangement.data.local.datastore.DataStoreManager
import com.productivity.fullmangement.data.local.datastore.SharedPrefKey
import com.productivity.fullmangement.data.local.datastore.SharedPrefValue

import com.productivity.fullmangement.data.repositories.Repository
import com.productivity.fullmangement.utils.converters.EnumDateTimeFormats
import com.productivity.fullmangement.utils.converters.asDomainModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: Repository,
    private val dataStoreManager: DataStoreManager,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val allTasksCount = repository.getTasksCount(DataBaseQueries.ALL_TASKS.query)
    private val expirationTasksCount = repository.getTasksCount(DataBaseQueries.EXPIRATION_DATE_TASKS.query)
    private val nearExpirationTasksCount = repository.getTasksCount(DataBaseQueries.NEAR_EXPIRATION_TASKS.query)
    private val completedTasksCount = repository.getTasksCount(DataBaseQueries.COMPLETED_TASKS.query)

    private val query = MutableStateFlow("")
    @ExperimentalCoroutinesApi
    private val tasksList = query.flatMapLatest { query ->
        repository.getTasks(TaskType.ALL, query)
    }
    fun onQueryChanged(newQuery: String){
        query.value = newQuery
    }

    @ExperimentalCoroutinesApi
    val homeUiState = combine(
        allTasksCount,
        expirationTasksCount,
        nearExpirationTasksCount,
        completedTasksCount,
    ){ all, expiration, nearExpiration, completed ->
        HomeUiState(
            allTasksCount = all,
            expirationTasksCount = expiration,
            nearExpirationTasksCount = nearExpiration,
            completedTasksCount = completed,
            tasksList = tasksList.map {
                it.asDomainModel(
                    context = context,
                    dateFormat = EnumDateTimeFormats.SHOW_DATE_TIME.format
                )
            }
        )
    }

    val isSeenOnBoarding = dataStoreManager.isSeenOnBoardingState.asLiveData()

    fun setSeenOnBoarding() = dataStoreManager.setIsSeenOnBoarding()

    val beforeOneDay = dataStoreManager.readValue(
        SharedPrefKey.isRemindBeforeOneDay,
        SharedPrefValue.DEFAULT_IS_REMIND_BEFORE_ONE_DAY
    )
    val beforeTwoHours = dataStoreManager.readValue(
        SharedPrefKey.isRemindBeforeTwoHours,
        SharedPrefValue.DEFAULT_IS_REMIND_BEFORE_TWO_HOURS
    )
    val whenDueDate = dataStoreManager.readValue(
        SharedPrefKey.isRemindWhenDueDate,
        SharedPrefValue.DEFAULT_IS_REMIND_WHEN_DUE_DATE
    )

    fun changeBeforeOneDay(state: Boolean) = viewModelScope.launch {
        dataStoreManager.storeValue(SharedPrefKey.isRemindBeforeOneDay, state)
    }
    fun changeBeforeTwoHours(state: Boolean) = viewModelScope.launch {
        dataStoreManager.storeValue(SharedPrefKey.isRemindBeforeTwoHours, state)
    }
    fun changeWhenDueDate(state: Boolean) = viewModelScope.launch {
        dataStoreManager.storeValue(SharedPrefKey.isRemindWhenDueDate, state)
    }

    fun changeSelectedLang(selectedLangCode: String) = viewModelScope.launch {
        dataStoreManager.storeValue(
            SharedPrefKey.selectedAppLanguage,
            selectedLangCode
        )
    }

    private val _homeUiActions = Channel<HomeActions>()
    val homeUiActions = _homeUiActions.receiveAsFlow()

    fun submitAction(action: HomeActions) {
        viewModelScope.launch {
            _homeUiActions.send(action)
        }
    }
}

@Immutable
data class HomeUiState(
    val allTasksCount: Int = 0,
    val expirationTasksCount: Int = 0,
    val nearExpirationTasksCount: Int = 0,
    val completedTasksCount: Int = 0,
    val tasksList: Flow<List<TaskDomain>> = emptyFlow()
){
    companion object {
        val Empty = HomeUiState()
    }
}

sealed class HomeActions{
    object OpenAddTaskAction: HomeActions()
    object OpenHomeAction: HomeActions()
    data class OpenTasksDetails(val taskId: Long): HomeActions()
    data class OpenTasksListAction(val taskType: TaskType): HomeActions()
}

