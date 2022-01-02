package com.productivity.fullmangement.ui.login

import android.content.Context
import androidx.lifecycle.*
import com.productivity.fullmangement.data.repositories.LoginRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.*
import javax.inject.Inject

@HiltViewModel
class LoginActivityViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: LoginRepository
) : ViewModel() {


    val isSeenOnBoarding = repository.isSeenOnBoardingState.asLiveData()


    private val _navigateToLogin = Channel<Boolean>()
    val navigateToLogin = _navigateToLogin.receiveAsFlow()
    fun onNavigateToLogin() = viewModelScope.launch {
        repository.setIsSeenOnBoarding()
        _navigateToLogin.send(true)
    }
}