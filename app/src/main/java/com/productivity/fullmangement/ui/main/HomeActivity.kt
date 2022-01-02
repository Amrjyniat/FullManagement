package com.productivity.fullmangement.ui.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.productivity.fullmangement.R
import com.productivity.fullmangement.databinding.ActivityHomeBinding
import com.productivity.fullmangement.ui.main.home.HomeViewModel
import com.productivity.fullmangement.utils.LocaleHelper
import com.productivity.fullmangement.utils.createChannel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val homeViewModel: HomeViewModel by viewModels()

    private val UPDATE_APP_REQUEST_CODE = 1212
    private lateinit var appUpdateManager: AppUpdateManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTheme(R.style.Theme_FullMangementCompose_NoActionBar)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        checkFirebase()
        checkForAppUpdated()

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_home) as NavHostFragment
        val navController = navHostFragment.navController

        Timber.i("TestLoginActivity onCreate()")

        homeViewModel.isSeenOnBoarding.observe(this) { isSeenOnBoarding ->
            Timber.i("TestLoginActivity isSeenOnBoarding: $isSeenOnBoarding")
            if (isSeenOnBoarding == true) {
                navController.popBackStack(R.id.onBoardingFragment, true)
                navController.navigate(R.id.HomeFragment)
            }
        }
    }

/*    private fun checkFirebase() {
        val auth: FirebaseAuth = Firebase.auth

        auth.createUserWithEmailAndPassword("amrjojo@gmail.com", "121212")
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = auth.currentUser
                    Timber.i("TestFirebase createUserWithEmail:success email: ${user?.email}")

                } else {
                    // If sign in fails, display a message to the user.
                    Timber.i("TestFirebase createUserWithEmail:failure: ${task.exception}")
                }
            }
    }*/

    private fun checkForAppUpdated() {
        appUpdateManager = AppUpdateManagerFactory.create(this)

        // Before starting an update, register a listener for updates.
        appUpdateManager.registerListener(listener)

        // Returns an intent object that you use to check for an update.
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

        // Checks that the platform will allow the specified type of update.
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->

            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                val updateType =
                    if (appUpdateInfo.updatePriority() >= 4 && appUpdateInfo.isUpdateTypeAllowed(
                            AppUpdateType.IMMEDIATE
                        )
                    ) {
                        AppUpdateType.IMMEDIATE
                    } else if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
                        && (appUpdateInfo.clientVersionStalenessDays() ?: -1) >= 7
                    ) {
                        AppUpdateType.FLEXIBLE
                    } else null

                updateType?.let {
                    appUpdateManager.startUpdateFlowForResult(
                        // Pass the intent that is returned by 'getAppUpdateInfo()'.
                        appUpdateInfo,
                        // Or 'AppUpdateType.FLEXIBLE' for flexible updates.
                        updateType,
                        // The current activity making the update request.
                        this,
                        // Include a request code to later monitor this update request.
                        UPDATE_APP_REQUEST_CODE
                    )
                }
            }

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == UPDATE_APP_REQUEST_CODE) {
            if (resultCode != RESULT_OK) {
                Timber.i("Update flow failed! Result code: $resultCode")
                // If the update is cancelled or fails,
                // you can request to start the update again.
            }
        }
    }

    // Create a listener to track request state updates.
    val listener = InstallStateUpdatedListener { state ->
        // (Optional) Provide a download progress bar.
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            popupSnackbarForCompleteUpdate()
        }
        // Log state or install the update.
    }


    // Displays the snackbar notification and call to action.
    fun popupSnackbarForCompleteUpdate() {
        Snackbar.make(
            binding.root,
            "An update has just been downloaded.",
            Snackbar.LENGTH_INDEFINITE
        ).apply {
            setAction("RESTART") { appUpdateManager.completeUpdate() }
            setActionTextColor(ContextCompat.getColor(context, R.color.colorPrimary))
            show()
        }
    }

    // Checks that the update is not stalled during 'onResume()'.
    // However, you should execute this check at all app entry points.
    override fun onResume() {
        super.onResume()

        appUpdateManager
            .appUpdateInfo
            .addOnSuccessListener { appUpdateInfo ->
                // If the update is downloaded but not installed,
                // notify the user to complete the update.
                if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                    popupSnackbarForCompleteUpdate()
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()

        // When status updates are no longer needed, unregister the listener.
        appUpdateManager.unregisterListener(listener)
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(base))
    }
}