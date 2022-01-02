package com.productivity.fullmangement.ui.login

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.productivity.fullmangement.R
import com.productivity.fullmangement.databinding.ActivityLoginBinding
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val loginViewModel: LoginActivityViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.login_nav_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        Timber.i("TestLoginActivity onCreate()")

        loginViewModel.isSeenOnBoarding.observe(this) { isSeenOnBoarding ->
            Timber.i("TestLoginActivity isSeenOnBoarding: $isSeenOnBoarding")
            if (isSeenOnBoarding == true) {
                navController.popBackStack(R.id.onBoardingFragment, true)
                navController.navigate(R.id.loginFragment)
            }
        }

    }
}