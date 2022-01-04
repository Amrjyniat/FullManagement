package com.productivity.fullmangement.ui.main.add_edit_task

import android.os.Bundle
import android.view.View
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.navigationBarsPadding
import com.productivity.fullmangement.R
import com.productivity.fullmangement.data.local.EnumPriorities
import com.productivity.fullmangement.data.local.EnumRepetition
import com.productivity.fullmangement.data.local.EnumTaskState
import com.productivity.fullmangement.ui.composables.*
import com.productivity.fullmangement.ui.theme.FullMangementComposeTheme
import com.productivity.fullmangement.utils.getActivity
import com.productivity.fullmangement.utils.showDatePicker
import com.productivity.fullmangement.utils.showReviewAppDialog
import com.productivity.fullmangement.utils.showTimePicker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class AddEditTaskFragment : Fragment(R.layout.fragment_add_edit_task) {

    private val viewModelAddTask: AddTaskViewModel by viewModels()

    @ExperimentalComposeUiApi
    @InternalCoroutinesApi
    @ExperimentalAnimationApi
    @ExperimentalUnitApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<ComposeView>(R.id.composeView).apply {
            // Dispose the Composition when the view's LifecycleOwner
            // is destroyed
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            setContent {
                FullMangementComposeTheme {
                    ProvideWindowInsets {
                        // A surface container using the 'background' color from the theme
                        val addEditTaskUiState by viewModelAddTask.addEditTaskUiState.collectAsState(
                            initial = AddEditTaskUiState.Empty
                        )
                        Surface(color = MaterialTheme.colors.background) {
                            Scaffold(
                                topBar = {
                                    CustomTopBar(
                                        title = if (addEditTaskUiState.taskId < 0)
                                            stringResource(R.string.add_task)
                                        else
                                            stringResource(R.string.edit_task),
                                        buttonIcon = rightBackIconDirection(),
                                        onButtonClicked = { findNavController().popBackStack() }
                                    )
                                },
                                floatingActionButton = {
                                    AnimatedVisibility(visible = !addEditTaskUiState.isLoading) {
                                        CustomFAB(Icons.Default.Done) {
                                            viewModelAddTask.onSaveClicked()
                                        }
                                    }
                                }
                            ) {
                                Container(addEditTaskUiState)
                            }
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModelAddTask.navigateBack.collect { createdTaskId ->
                    if (createdTaskId.toInt() % 5 == 0 && viewModelAddTask.taskId < 1){
                        showReviewAppDialog(requireActivity()){
                            findNavController().popBackStack()
                        }
                    } else {
                        findNavController().popBackStack()
                    }
                }
            }
        }
    }


    @ExperimentalComposeUiApi
    @ExperimentalAnimationApi
    @ExperimentalUnitApi
    @Composable
    private fun Container(addEditTaskUiState: AddEditTaskUiState = AddEditTaskUiState.Empty) {
        AnimatedVisibility(visible = addEditTaskUiState.isLoading) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
        }

        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            CustomInputTextWithTitleAndError(
                textTitle = stringResource(R.string.task_name_required),
                hint = stringResource(R.string.enter_task_name),
                errorResStringId = addEditTaskUiState.errorTaskNameResId,
                textValue = addEditTaskUiState.name,
                onValueChanged = { newValue ->
                    Timber.i("TestText value fragment: $newValue")
                    viewModelAddTask.onTaskNameChanged(newValue)
                },
                modifier = Modifier.navigationBarsPadding()
            )

            CustomInputTextWithTitleAndError(
                textTitle = stringResource(R.string.description),
                hint = stringResource(R.string.enter_task_description),
                textValue = addEditTaskUiState.description,
                onValueChanged = { newValue -> viewModelAddTask.onTaskDescChanged(newValue) },
                modifier = Modifier.navigationBarsPadding()
            )

            val activity = LocalContext.current.getActivity()
            val keyboardController = LocalSoftwareKeyboardController.current
            CustomInputDateWithTitle(
                textTitle = stringResource(R.string.due_date),
                value = addEditTaskUiState.dueDate,
                onPickerClicked = {
                    keyboardController?.hide()
                    showDatePicker(activity, false) { dateInMills ->
                        showTimePicker(activity) { timePickerToBuilder ->
                            val hour = timePickerToBuilder.hour.toLong()
                            val minutes = timePickerToBuilder.minute.toLong()
                            val dateTimeInMill = dateInMills.plus(
                                TimeUnit.HOURS.toMillis(hour)
                            ).plus(
                                TimeUnit.MINUTES.toMillis(minutes)
                            )
                            viewModelAddTask.setEndDate(dateTimeInMill)
                        }
                    }
                }
            )

            CustomDropDownMenuWithTitle(
                textTitle = stringResource(R.string.priority),
                items = EnumPriorities.values().map { stringResource(it.resIdPriorityName) },
                selectedItem = stringResource(addEditTaskUiState.priority.resIdPriorityName),
                itemsColor = EnumPriorities.values().map { it.color },
                selectedColor = addEditTaskUiState.priority.color,
            ) { string ->
                val priority = EnumPriorities.values().singleOrNull { getString(it.resIdPriorityName) == string }
                priority?.let { viewModelAddTask.setPriority(it) }
            }

            CustomDropDownMenuWithTitle(
                textTitle = stringResource(R.string.task_state),
                items = EnumTaskState.values().map { stringResource(it.resIdTaskStateName) },
                selectedItem = stringResource(addEditTaskUiState.taskState.resIdTaskStateName),
            ) { string ->
                val taskState = EnumTaskState.values().singleOrNull { getString(it.resIdTaskStateName) == string }
                taskState?.let { viewModelAddTask.setTaskState(it) }
            }

            CustomDropDownMenuWithTitle(
                textTitle = stringResource(R.string.repetition),
                items = EnumRepetition.values().map { stringResource(id = it.resIdRepetitionName) },
                selectedItem = stringResource(addEditTaskUiState.repetition.resIdRepetitionName),
            ) { string ->
                val repetition = EnumRepetition.values().singleOrNull { getString(it.resIdRepetitionName) == string }
                repetition?.let { viewModelAddTask.setRepetition(it) }
            }


            CheckBoxWithTitle(
                stringResource(R.string.archive_the_task_after_its_completed),
                addEditTaskUiState.isArchivedAfterCompleted,
                Modifier.padding(vertical = 6.dp)
            ){
                viewModelAddTask.onChangeArchivedAfterCompleted()
            }

        }
    }

    @ExperimentalComposeUiApi
    @ExperimentalAnimationApi
    @ExperimentalUnitApi
    @Preview(showBackground = true)
    @Composable
    fun PreviewContainer() {
        FullMangementComposeTheme {
            Container()
        }
    }

}
