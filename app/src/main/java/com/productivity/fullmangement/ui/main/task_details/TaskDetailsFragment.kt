package com.productivity.fullmangement.ui.main.task_details

import android.os.Bundle
import android.view.View
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.productivity.fullmangement.R
import com.productivity.fullmangement.data.domain.TaskDomain
import com.productivity.fullmangement.ui.composables.CustomFAB
import com.productivity.fullmangement.ui.composables.CustomTopBar
import com.productivity.fullmangement.ui.composables.ShowSureDeleteTaskDialog
import com.productivity.fullmangement.ui.composables.rightBackIconDirection
import com.productivity.fullmangement.ui.theme.FullMangementComposeTheme
import com.productivity.fullmangement.ui.theme.texDetailsStyle
import com.productivity.fullmangement.ui.theme.texTitleStyle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TaskDetailsFragment : Fragment(R.layout.task_details_fragment) {

    private val viewModel: TaskDetailsViewModel by viewModels()

    var showSureDeleteTaskDialog by mutableStateOf(false)
        private set

    @ExperimentalFoundationApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<ComposeView>(R.id.composeView).apply {
            // Dispose the Composition when the view's LifecycleOwner
            // is destroyed
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            setObservers()

            setContent {
                FullMangementComposeTheme {
                    // A surface container using the 'background' color from the theme
                    Surface(color = MaterialTheme.colors.background) {
                        Scaffold(
                            topBar = {
                                CustomTopBar(
                                    title = stringResource(id = R.string.task_details),
                                    buttonIcon = rightBackIconDirection(),
                                    onButtonClicked = { findNavController().popBackStack() },
                                    actions = {
                                        IconButton(
                                            onClick = {
                                                viewModel.submitAction(TaskDetailsActions.ShowSureDeleteTaskDialog)
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete task",
                                                tint = Color.White
                                            )
                                        }
                                    }
                                )
                            },
                            floatingActionButton = {
                                CustomFAB(
                                    Icons.Default.Edit
                                ) {
                                    viewModel.submitAction(TaskDetailsActions.OpenEditTask(viewModel.taskId))
                                }
                            }
                        ) {
                            val task by viewModel.task.collectAsState(TaskDomain.Empty)
                            Container(task)
                            if (showSureDeleteTaskDialog){
                                ShowSureDeleteTaskDialog(
                                    onConfirmButton = {
                                        viewModel.deleteTask()
                                        showSureDeleteTaskDialog = false
                                    },
                                    onDismissButton = {
                                        showSureDeleteTaskDialog = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

    }

    private fun setObservers() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.taskDetailsActions.collect { action ->
                    when(action){
                        is TaskDetailsActions.OpenEditTask -> {
                            findNavController().navigate(TaskDetailsFragmentDirections.toAddEditTaskFragment(action.taskId))
                        }
                        is TaskDetailsActions.ShowSureDeleteTaskDialog -> {
                            showSureDeleteTaskDialog = true
                        }
                        is TaskDetailsActions.NavigateBack -> {
                            findNavController().popBackStack()
                        }
                    }
                }
            }
        }
    }

    @ExperimentalFoundationApi
    @Composable
    private fun Container(task: TaskDomain) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = stringResource(R.string.task_name),
                style = texTitleStyle,
                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
                color = MaterialTheme.colors.primary
            )
            Text(
                text = task.title,
                style = texDetailsStyle
            )

            Text(
                text = stringResource(R.string.description),
                style = texTitleStyle,
                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
                color = MaterialTheme.colors.primary
            )
            Text(
                text = task.description.orEmpty(),
                style = texDetailsStyle
            )

            val titles = mapOf(
                stringResource(id = R.string.priority) to getString(task.priority.resIdPriorityName),
                stringResource(id = R.string.due_date) to task.dueDateTime.orEmpty(),
                stringResource(id = R.string.task_state) to getString(task.taskState.resIdTaskStateName),
                stringResource(id = R.string.repetition) to getString(task.repetition.resIdRepetitionName)
            )

            LazyVerticalGrid(
                cells = GridCells.Fixed(2),
                modifier = Modifier.padding(top = 16.dp)
            ) {
                itemsIndexed(titles.keys.toList()) { idx, title ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = if (idx >= 2) 16.dp else 0.dp)
                    ) {
                        Text(
                            text = title,
                            style = texTitleStyle,
                            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
                            color = MaterialTheme.colors.primary
                        )
                        Text(
                            text = titles[title].orEmpty(),
                            textAlign = TextAlign.Center,
                            style = texDetailsStyle
                        )
                    }
                }
            }
        }
    }

    @ExperimentalFoundationApi
    @ExperimentalAnimationApi
    @ExperimentalUnitApi
    @Preview(showBackground = true)
    @Composable
    fun PreviewContainer() {
        FullMangementComposeTheme {
            Container(TaskDomain.Preview)
        }
    }


}