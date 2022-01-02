package com.productivity.fullmangement.ui.main.tasks_list

import ChipGroupMultiSelection
import android.os.Bundle
import android.view.View
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.play.core.review.ReviewManagerFactory
import com.productivity.fullmangement.R
import com.productivity.fullmangement.data.local.*
import com.productivity.fullmangement.ui.composables.*
import com.productivity.fullmangement.ui.theme.Blue600
import com.productivity.fullmangement.ui.theme.FullMangementComposeTheme
import com.productivity.fullmangement.ui.theme.GrayWhite
import com.productivity.fullmangement.ui.theme.texTitleStyle
import com.productivity.fullmangement.utils.*
import com.productivity.fullmangement.utils.converters.EnumDateTimeFormats
import com.productivity.fullmangement.utils.converters.getDateFormattedFromMills
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TasksFragment : Fragment(R.layout.tasks_fragment) {

    private val viewModel: TasksViewModel by viewModels()

    var taskIdToDelete by mutableStateOf<Long>(-1)
        private set

    @ExperimentalMaterialApi
    @ExperimentalAnimationApi
    @ExperimentalUnitApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setObservers()

        view.findViewById<ComposeView>(R.id.composeView).apply {
            // Dispose the Composition when the view's LifecycleOwner
            // is destroyed
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            setContent {
                FullMangementComposeTheme {
                    // A surface container using the 'background' color from the theme
                    Surface(color = MaterialTheme.colors.background) {
                        val tasksUiState by viewModel.tasksUiState.collectAsState()
                        val coroutineScope = rememberCoroutineScope()

                        val bottomState = rememberModalBottomSheetState(
                            initialValue = ModalBottomSheetValue.Hidden,
                            confirmStateChange = { stateValue ->
                                stateValue != ModalBottomSheetValue.HalfExpanded
                            }
                        )

                        BackHandler(bottomState.isVisible) {
                            coroutineScope.launch { bottomState.hide() }
                        }

                        ModalBottomSheetLayout(
                            sheetState = bottomState,
                            sheetContent = {
                                BottomSheetFilter(bottomState, coroutineScope, tasksUiState)
                                //reset the filters to the previous applied filters if the user close the dialog without clicking show results button
                                LaunchedEffect(bottomState.currentValue) {
                                    if (bottomState.currentValue == ModalBottomSheetValue.Hidden) {
                                        viewModel.applyFilters(tasksUiState.filtersToApply)
                                    }
                                }
                            },
                            sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                        ) {
                            Scaffold(
                                topBar = {
                                    var expanded by rememberSaveable { mutableStateOf(false) }
                                    if (!expanded) {
                                        CustomTopBar(
                                            title = stringResource(id = tasksUiState.tasksType.readableStringResId),
                                            buttonIcon = rightBackIconDirection(),
                                            onButtonClicked = { findNavController().popBackStack() },
                                            actions = {
                                                IconButton(
                                                    onClick = {
                                                        expanded = true
                                                    }
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Search,
                                                        contentDescription = "search",
                                                        tint = Color.White
                                                    )
                                                }
                                                IconButton(
                                                    onClick = {
                                                        coroutineScope.launch {
                                                            bottomState.animateTo(
                                                                ModalBottomSheetValue.Expanded
                                                            )
                                                        }
                                                    }
                                                ) {
                                                    val iconFilterState =
                                                        if (tasksUiState.filtersToApply.isThereAnyFilterApplied())
                                                            R.drawable.ic_filter_off
                                                        else
                                                            R.drawable.ic_filter
                                                    Icon(
                                                        painter = painterResource(id = iconFilterState),
                                                        contentDescription = "Filter",
                                                        tint = Color.Unspecified
                                                    )
                                                }
                                            }
                                        )
                                    } else {
                                        CustomSearchBar {
                                            coroutineScope.launch {
                                                delay(50)
                                                expanded = false
                                            }
                                        }
                                    }
                                },
                                floatingActionButton = {
                                    CustomFAB(
                                        Icons.Default.Add
                                    ) {
                                        viewModel.submitAction(TasksActions.OpenAddTaskAction)
                                    }
                                }
                            ) {
                                Container(tasksUiState)

                                if (taskIdToDelete > 0) {
                                    ShowSureDeleteTaskDialog(
                                        onConfirmButton = {
                                            viewModel.deleteTask(taskIdToDelete)
                                            taskIdToDelete = -1
                                        },
                                        onDismissButton = {
                                            taskIdToDelete = -1
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @ExperimentalAnimationApi
    @Composable
    private fun Container(tasksUiState: TasksUiState = TasksUiState.Empty) {

        if (tasksUiState.tasksList.isEmpty()) {
            val textValue = when {
                tasksUiState.filtersToApply.isThereAnyFilterApplied() ||
                        tasksUiState.query.isNotEmpty() -> {
                    stringResource(R.string.there_are_no_results)
                }
                tasksUiState.tasksType == TaskType.ARCHIVED -> stringResource(R.string.list_empty_archive)
                else -> stringResource(R.string.list_empty)
            }

            EmptyList(textValue){
                if (tasksUiState.filtersToApply.isThereAnyFilterApplied()) {
                    Button(
                        onClick = {
                            viewModel.clearAppliedFilters()
                        },
                        colors = ButtonDefaults.buttonColors(Blue600)
                    ) {
                        Text(
                            text = stringResource(id = R.string.remove_filters),
                            style = texTitleStyle
                        )
                    }
                }
            }
        } else {
            TasksList(tasksUiState)
        }
    }




    @ExperimentalAnimationApi
    @Composable
    private fun TasksList(tasksUiState: TasksUiState = TasksUiState.Empty) {
        val revealTasksId = tasksUiState.revealTasksId.collectAsState(listOf())

        LazyColumn(
            Modifier
                .fillMaxSize()
                .background(color = GrayWhite)
        ) {
            itemsIndexed(tasksUiState.tasksList, key = { _, item -> item.taskId }) { _, task ->
                SwipeableCardTask(
                    task = task,
                    revealTasksId = revealTasksId,
                    taskType = tasksUiState.tasksType,
                    onArchivedClick = { doArchiveOrUnarchive(task.taskId, tasksUiState.tasksType) },
                    onDeleteClick = {
                        viewModel.submitAction(
                            TasksActions.ShowSureDeleteTaskDialog(
                                task.taskId
                            )
                        )
                    },
                    onExpand = { viewModel.onItemExpanded(task.taskId.toInt()) },
                    onCollapse = { viewModel.onItemCollapsed(task.taskId.toInt()) },
                    onClickCard = { viewModel.submitAction(TasksActions.OpenTaskDetails(task.taskId)) }
                )
            }
        }
    }


    @ExperimentalMaterialApi
    @Composable
    private fun BottomSheetFilter(
        state: ModalBottomSheetState,
        scope: CoroutineScope,
        tasksUiState: TasksUiState = TasksUiState.Empty
    ) {
        val scrollState = rememberScrollState()

        Box(
            modifier = Modifier
                .fillMaxHeight(0.9f),
        ) {
            Column(Modifier.fillMaxSize()) {
                Divider(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(75.dp)
                        .padding(top = 8.dp),
                    color = Color.Gray,
                    thickness = 4.dp,
                )
                IconButton(onClick = {
                    scope.launch { state.hide() }
                }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = Color.Gray
                    )
                }


                Column(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 10.dp)
                ) {
                    Spacer(modifier = Modifier.padding(vertical = 6.dp))

                    val prioritiesName = tasksUiState.filters.prioritiesId.getPriNamesFromIds(requireContext())
                    ExpandableCard(
                        title = stringResource(id = R.string.priority),
                    ) {
                        ChipGroupMultiSelection(
                            items = EnumPriorities.values().map { stringResource(id = it.resIdPriorityName) },
                            selectedItems = prioritiesName,
                            onSelectedChanged = { selectedVal ->
                                val selectedPriorities =
                                    addOrRemoveFromList(prioritiesName.toMutableList(), selectedVal)
                                viewModel.applyFilters(tasksUiState.filters.copy(prioritiesId = selectedPriorities.getPriIdsFromNames(requireContext())))

                            }
                        )
                    }
                    Spacer(modifier = Modifier.padding(vertical = 6.dp))

                    val taskStateNames = tasksUiState.filters.taskStates.getTaskStateNamesFromIds(requireContext())
                    ExpandableCard(
                        title = stringResource(id = R.string.task_state),
                    ) {
                        ChipGroupMultiSelection(
                            items = EnumTaskState.values().map { stringResource(id = it.resIdTaskStateName) },
                            selectedItems = taskStateNames,
                            onSelectedChanged = { selectedVal ->
                                val selectedTaskStates =
                                    addOrRemoveFromList(taskStateNames.toMutableList(), selectedVal)
                                viewModel.applyFilters(tasksUiState.filters.copy(taskStates = selectedTaskStates.getTaskStateIdsFromNames(requireContext())))
                            }
                        )
                    }

                    val repetitionNames =
                        tasksUiState.filters.repetitions.getRepetitionNamesFromIds(requireContext())
                    Spacer(modifier = Modifier.padding(vertical = 6.dp))
                    ExpandableCard(title = stringResource(id = R.string.repetition)) {
                        ChipGroupMultiSelection(
                            items = EnumRepetition.values().map { stringResource(id = it.resIdRepetitionName) },
                            selectedItems = repetitionNames,
                            onSelectedChanged = { selectedVal ->
                                val selectedRepetition =
                                    addOrRemoveFromList(
                                        repetitionNames.toMutableList(),
                                        selectedVal
                                    )
                                viewModel.applyFilters(tasksUiState.filters.copy(repetitions = selectedRepetition.getRepetitionIdsFromNames(requireContext())))

                            }
                        )
                    }

                    Spacer(modifier = Modifier.padding(vertical = 6.dp))

                    ExpandableCard(
                        title = stringResource(id = R.string.due_date),
                    ) {
                        DateFilterSection(tasksUiState, scrollState)
                    }
                }
                Row(
                    Modifier
                        .padding(
                            vertical = 10.dp,
                            horizontal = 20.dp
                        )
                        .fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            viewModel.showResults()
                            scope.launch {
                                state.hide()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = MaterialTheme.colors.primary,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .weight(1f),
                        shape = RoundedCornerShape(28.dp),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.show_results),
                            fontSize = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    Button(
                        onClick = {
                            viewModel.clearFilters()
                        },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color.LightGray,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier
                            .weight(1f),
                        shape = RoundedCornerShape(28.dp),
                        contentPadding = PaddingValues(vertical = 13.dp),
                        enabled = tasksUiState.filters.isThereAnyFilterApplied()
                    ) {
                        Text(
                            text = stringResource(R.string.clear_all),
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun DateFilterSection(
        tasksUiState: TasksUiState,
        scrollState: ScrollState
    ) {
        Column {

            RadioGroup(
                radioOptions = PeriodsDateFilterEnum.values()
                    .map { stringResource(id = it.stringResId) },
                selectedOption = stringResource(id = tasksUiState.filters.periodsDateFilter.titleStringResId),
                onOptionSelected = { newValue ->
                    val selectedPeriod = when (newValue) {
                        getString(PeriodsDateFilterEnum.TODAY.stringResId) -> {
                            PeriodsDateFilter.Today()
                        }
                        getString(PeriodsDateFilterEnum.TOMORROW.stringResId) -> {
                            PeriodsDateFilter.Tomorrow()
                        }
                        getString(PeriodsDateFilterEnum.THIS_WEEK.stringResId) -> {
                            PeriodsDateFilter.ThisWeek()
                        }
                        getString(PeriodsDateFilterEnum.THIS_MONTH.stringResId) -> {
                            PeriodsDateFilter.ThisMonth()
                        }
                        getString(PeriodsDateFilterEnum.CUSTOM_DATE.stringResId) -> {
                            PeriodsDateFilter.CustomDate()
                        }
                        else -> {
                            PeriodsDateFilter.Nothing()
                        }
                    }
                    viewModel.applyFilters(
                        tasksUiState.filters.copy(
                            periodsDateFilter = selectedPeriod
                        )
                    )
                }
            )


            AnimatedVisibility(tasksUiState.filters.periodsDateFilter is PeriodsDateFilter.CustomDate) {
                if (tasksUiState.filters.periodsDateFilter is PeriodsDateFilter.CustomDate) {
                    val customDate = tasksUiState.filters.periodsDateFilter
                    val fromDateValue = customDate.fromDate.getDateFormattedFromMills(
                        EnumDateTimeFormats.NORMAL_DATE.format
                    ).orEmpty()
                    val toDateValue = customDate.toDate.getDateFormattedFromMills(
                        EnumDateTimeFormats.NORMAL_DATE.format
                    ).orEmpty()

                    SelectCustomDate(
                        scrollState = scrollState,
                        fromDate = fromDateValue,
                        toDate = toDateValue,
                        onSelectedFromDate = { dateInMills ->
                            viewModel.applyFilters(
                                tasksUiState.filters.copy(
                                    periodsDateFilter = customDate.copy(
                                        fromDate = dateInMills
                                    )
                                )
                            )
                        },
                        onSelectedToDate = { dateInMills ->
                            viewModel.applyFilters(
                                tasksUiState.filters.copy(
                                    periodsDateFilter = customDate.copy(
                                        toDate = dateInMills
                                    )
                                )
                            )
                        }
                    )
                }
            }
        }
    }

    @Composable
    fun CustomSearchBar(
        onCloseExpand: () -> Unit
    ) {
        val text = ""
        var textFieldValueState by rememberSaveable(stateSaver = TextFieldValue.Saver) {
            mutableStateOf(
                TextFieldValue(
                    text = text,
                    selection = TextRange(
                        index = text.length
                    )
                )
            )
        }
        val focusRequester = remember { FocusRequester() }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colors.primary
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                textFieldValueState = TextFieldValue()
                viewModel.onQueryChanged("")
                onCloseExpand()
            }) {
                Icon(
                    imageVector = rightBackIconDirection(),
                    contentDescription = "back",
                    tint = Color.White
                )
            }
            TextField(
                value = textFieldValueState,
                onValueChange = { newText ->
                    textFieldValueState = newText
                    viewModel.onQueryChanged(newText.text)
                },
                placeholder = {
                    Text(
                        text = stringResource(R.string.search_dots),
                        color = Color.LightGray
                    )
                },
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.Transparent,
                    cursorColor = Color.LightGray,
                    textColor = Color.White
                ),
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
            )
            DisposableEffect(Unit) {
                focusRequester.requestFocus()
                onDispose { }
            }
            AnimatedVisibility(visible = textFieldValueState.text.isNotEmpty()) {
                IconButton(
                    onClick = {
                        textFieldValueState = TextFieldValue()
                        viewModel.onQueryChanged("")
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close search",
                        tint = Color.White
                    )
                }
            }
        }
    }

    @ExperimentalAnimationApi
    @Preview(showBackground = true)
    @Composable
    private fun DefaultPreview() {
        Container()
    }

    private fun setObservers() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.tasksActions.collect { action ->
                    when (action) {
                        is TasksActions.OpenAddTaskAction -> {
                            findNavController().navigate(TasksFragmentDirections.toAddEditTask())
                        }
                        is TasksActions.ShowSureDeleteTaskDialog -> {
                            taskIdToDelete = action.taskId
                        }
                        is TasksActions.OpenTaskDetails -> {
                            findNavController().navigate(
                                TasksFragmentDirections.toTaskDetails(
                                    action.taskId
                                )
                            )
                        }
                    }
                }
            }
        }
    }


    private fun doArchiveOrUnarchive(taskId: Long, tasksType: TaskType) {
        if (tasksType == TaskType.ARCHIVED) {
            viewModel.unarchiveTask(taskId)
        } else {
            viewModel.archiveTask(taskId)
        }
    }

}
