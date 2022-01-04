package com.productivity.fullmangement.ui.main.home

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.ConfigurationCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.accompanist.insets.ProvideWindowInsets
import com.productivity.fullmangement.FullManagementApplication
import com.productivity.fullmangement.R
import com.productivity.fullmangement.data.domain.TaskDomain
import com.productivity.fullmangement.data.local.EnumLanguage
import com.productivity.fullmangement.data.local.TaskType
import com.productivity.fullmangement.data.local.datastore.SharedPrefKey
import com.productivity.fullmangement.ui.composables.*
import com.productivity.fullmangement.ui.main.HomeActivity
import com.productivity.fullmangement.ui.theme.*
import com.productivity.fullmangement.utils.*
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.MaterialDialogState
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private val viewModelHome: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setObserversNavigation()

        //Navigate to task details when came from notification
        val taskId = requireActivity().intent.extras?.getLong("taskId") ?: -1
        Timber.i("TestAlarm onCreate() taskId: $taskId")
        if (taskId > 0) {
            viewModelHome.submitAction(HomeActions.OpenTasksDetails(taskId))
        }

        //store current app language into dataStore
        val currLangCode = getString(requireContext().getCurrLanguage().languageCode)
        Timber.i("TestAlarm onCreate() currLangCode: $currLangCode")
        viewModelHome.changeSelectedLang(currLangCode)

//        sendNotification("test", "some desc", 1, requireContext())
    }

    @ExperimentalCoroutinesApi
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

                        val scaffoldState = rememberScaffoldState()
                        val coroutineScope = rememberCoroutineScope()

                        // A surface container using the 'background' color from the theme
                        Surface(color = MaterialTheme.colors.background) {
                            Scaffold(
                                topBar = {
                                    CustomTopBar(
                                        title = stringResource(id = R.string.app_name),
                                        buttonIcon = Icons.Default.Menu,
                                        onButtonClicked = { coroutineScope.launch { scaffoldState.drawerState.open() } }
                                    )
                                },
                                scaffoldState = scaffoldState,
                                drawerContent = {
                                    NavDrawer("") {
                                        coroutineScope.launch { scaffoldState.drawerState.close() }
                                    }
                                },
                                floatingActionButton = {
                                    CustomFAB(
                                        Icons.Default.Add
                                    ) {
                                        viewModelHome.submitAction(HomeActions.OpenAddTaskAction)
                                    }
                                }
                            ) {
                                val homeUiState by viewModelHome.homeUiState.collectAsState(
                                    HomeUiState.Empty
                                )
                                Container(homeUiState)
                            }
                        }
                    }
                }
            }
        }
    }

    @ExperimentalUnitApi
    @Composable
    fun Container(homeUiState: HomeUiState = HomeUiState.Empty) {
        Column(
            modifier = Modifier.fillMaxSize(),
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
            var haveFocus by remember { mutableStateOf(false) }

            val focusRequester = remember { FocusRequester() }
            val focusManager = LocalFocusManager.current

            if (textFieldValueState.text.isNotEmpty()) {
                DisposableEffect(Unit) {
                    focusRequester.requestFocus()
                    onDispose { }
                }
            }

            OutlinedTextField(
                value = textFieldValueState,
                onValueChange = { newText ->
                    textFieldValueState = newText
                    viewModelHome.onQueryChanged(newText.text)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
                    .onFocusChanged { focusState ->
                        haveFocus = focusState.isFocused
                    }
                    .focusRequester(focusRequester),
                placeholder = {
                    Text(
                        text = stringResource(id = R.string.search_dots),
                        style = textHintStyle
                    )
                },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = MaterialTheme.colors.secondary,
                    unfocusedBorderColor = MaterialTheme.colors.secondary,
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
            )

            BackHandler(true) {
                if (haveFocus) {
                    focusManager.clearFocus()
                    textFieldValueState = TextFieldValue()
                    viewModelHome.onQueryChanged("")
                } else {
                    requireActivity().finish()
                }
            }

            val tasksList by homeUiState.tasksList.collectAsState(initial = listOf())
            if (haveFocus) {
                TasksListSection(tasksList)
            } else {
                TaskSections(homeUiState)
            }

        }
    }

    @Composable
    private fun ColumnScope.TaskSections(homeUiState: HomeUiState) {
        TasksStateSection(
            AllTasksSection,
            stringResource(id = R.string.all_tasks_dynamic, homeUiState.allTasksCount)
        ) {
            viewModelHome.submitAction(HomeActions.OpenTasksListAction(TaskType.ALL))
        }

        TasksStateSection(
            OutDateTasksSection,
            stringResource(id = R.string.tasks_out_date_dynamic, homeUiState.expirationTasksCount)
        ) {
            viewModelHome.submitAction(HomeActions.OpenTasksListAction(TaskType.EXPIRATION))
        }
        TasksStateSection(
            NearOutDateTasksSection,
            stringResource(
                id = R.string.tasks_near_out_date_dynamic,
                homeUiState.nearExpirationTasksCount
            )
        ) {
            viewModelHome.submitAction(HomeActions.OpenTasksListAction(TaskType.NEAR_EXPIRATION))
        }
        TasksStateSection(
            CompletedTasksSection,
            stringResource(id = R.string.tasks_completed_dynamic, homeUiState.completedTasksCount)
        ) {
            viewModelHome.submitAction(HomeActions.OpenTasksListAction(TaskType.COMPLETED))
        }

    }

    @Composable
    private fun TasksListSection(tasksList: List<TaskDomain>) {
        if(tasksList.isEmpty()) {
            EmptyList(stringResource(R.string.list_empty))
        } else {
            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .background(color = GrayWhite)
            ) {
                itemsIndexed(tasksList, key = { _, item -> item.taskId }) { _, task ->
                    ItemCardTask(
                        task = task,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    ) {
                        viewModelHome.submitAction(
                            HomeActions.OpenTasksDetails(
                                task.taskId
                            )
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun ColumnScope.TasksStateSection(
        colorSection: Color,
        titleSection: String,
        onSectionClick: () -> Unit
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(color = colorSection)
                .clickable { onSectionClick() }
        ) {
            Text(
                text = titleSection,
                color = Color.White,
                style = MaterialTheme.typography.h5,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }

    //region Drawer code
    @Composable
    fun NavDrawer(userName: String, onDrawerOptionClick: () -> Unit) {
        Column {
            DrawerHeader(userName)
            DrawerListOptions(onDrawerOptionClick)
            MadeByAmr()
        }
    }

    @Composable
    fun DrawerHeader(userName: String) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.2f)
                .background(color = MaterialTheme.colors.primary)
                .padding(16.dp),
            contentAlignment = Alignment.BottomStart
        ) {
            Text(
                text = userName,
                color = Color.White,
                style = MaterialTheme.typography.h5
            )
        }
    }

    @Composable
    fun ColumnScope.DrawerListOptions(onDrawerOptionClick: () -> Unit) {
        Column(
            modifier = Modifier
                .padding(vertical = 16.dp)
                .weight(1f)
        ) {
            DrawerOption(
                title = stringResource(R.string.archived_tasks),
                buttonIcon = painterResource(id = R.drawable.ic_archive),
                onButtonClicked = {
                    onDrawerOptionClick()
                    viewModelHome.submitAction(HomeActions.OpenTasksListAction(TaskType.ARCHIVED))
                }
            )
            val languageDialogState = rememberMaterialDialogState(false)
            DrawerOption(
                title = stringResource(R.string.switch_language),
                buttonIcon = painterResource(id = R.drawable.ic_language),
                onButtonClicked = {
                    languageDialogState.show()
                    onDrawerOptionClick()
                }
            )

            ChangeLanguageDialog(languageDialogState)

            val notificationsDialogState = rememberMaterialDialogState(false)
            DrawerOption(
                title = stringResource(R.string.notifications),
                buttonIcon = painterResource(id = R.drawable.ic_notification),
                onButtonClicked = {
                    notificationsDialogState.show()
                    onDrawerOptionClick()
                }
            )

            NotificationDialog(notificationsDialogState)

            DrawerOption(
                title = stringResource(R.string.share_app),
                buttonIcon = painterResource(id = R.drawable.ic_share),
                onButtonClicked = {
                    shareApp()
                    onDrawerOptionClick()
                }
            )
            DrawerOption(
                title = stringResource(R.string.rate_app),
                buttonIcon = painterResource(id = R.drawable.ic_like),
                onButtonClicked = {
                    rateApp()
                    onDrawerOptionClick()
                }
            )
            DrawerOption(
                title = stringResource(R.string.contact_us),
                buttonIcon = painterResource(id = R.drawable.ic_feedback),
                onButtonClicked = {
                    contactMe()
                    onDrawerOptionClick()
                }
            )

            /*DrawerOption(
                title = stringResource(R.string.sign_out),
                buttonIcon = painterResource(id = R.drawable.ic_log_out),
                onButtonClicked = onDrawerOptionClick
            )*/
        }
    }

    @Composable
    fun NotificationDialog(
        dialogState: MaterialDialogState
    ) {
        MaterialDialog(dialogState = dialogState) {
            Column(Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = stringResource(id = R.string.notifications),
                    fontSize = 22.sp,
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                CheckBoxWithTitle(
                    stringResource(R.string.notify_before_one_day),
                    viewModelHome.beforeOneDay.collectAsState(initial = true).value
                ) {
                    viewModelHome.changeBeforeOneDay(it)
                }
                CheckBoxWithTitle(
                    stringResource(R.string.notify_before_two_hours),
                    viewModelHome.beforeTwoHours.collectAsState(initial = true).value
                ) {
                    viewModelHome.changeBeforeTwoHours(it)
                }
                CheckBoxWithTitle(
                    stringResource(R.string.notify_when_due_date),
                    viewModelHome.whenDueDate.collectAsState(initial = true).value
                ) {
                    viewModelHome.changeWhenDueDate(it)
                }

                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            dialogState.hide()
                        }) {
                        Text(text = stringResource(id = R.string.done))
                    }
                }
            }
        }
    }

    @Composable
    fun DrawerOption(title: String, buttonIcon: Painter, onButtonClicked: () -> Unit) {
        Row(
            Modifier
                .fillMaxWidth()
                .clickable {
                    onButtonClicked()
                }
                .padding(15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = buttonIcon,
                modifier = Modifier.size(30.dp),
                tint = Color.Gray,
                contentDescription = null
            )
            Spacer(modifier = Modifier.padding(15.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.W600),
            )
        }
    }

    @Composable
    fun MadeByAmr() {
        val annotatedLinkString: AnnotatedString = buildAnnotatedString {
            val name = "Amr Jyniat"
            val str = "Made by $name"
            val startIndex = str.indexOf(name)
            val endIndex = startIndex + name.length
            append(str)
            addStyle(
                style = SpanStyle(
                    color = MaterialTheme.colors.primary,
                    fontSize = 18.sp,
                    textDecoration = TextDecoration.Underline
                ), start = startIndex, end = endIndex
            )

            // attach a string annotation that stores a URL to the text "link"
            addStringAnnotation(
                tag = "URL",
                annotation = "https://www.linkedin.com/in/amralgnyat/",
                start = startIndex,
                end = endIndex
            )
        }

        // UriHandler parse and opens URI inside AnnotatedString Item in Browse
        val uriHandler = LocalUriHandler.current

        // 🔥 Clickable text returns position of text that is clicked in onClick callback
        ClickableText(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            text = annotatedLinkString,
            onClick = {
                annotatedLinkString
                    .getStringAnnotations("URL", it, it)
                    .firstOrNull()?.let { stringAnnotation ->
                        uriHandler.openUri(stringAnnotation.item)
                    }
            }
        )
    }

    //endregion

    @Composable
    fun ChangeLanguageDialog(
        dialogState: MaterialDialogState
    ) {
        MaterialDialog(dialogState = dialogState) {
            Column(Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = stringResource(id = R.string.switch_language),
                    fontSize = 22.sp,
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                val options = EnumLanguage.values().map { stringResource(id = it.resStringId) }
                val currLang = getString(requireContext().getCurrLanguage().resStringId)
                var selectedLanguage by remember { mutableStateOf(currLang) }

                RadioGroup(
                    radioOptions = options,
                    selectedOption = selectedLanguage,
                    onOptionSelected = { newVal -> selectedLanguage = newVal }
                )
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            dialogState.hide()
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.LightGray)
                    ) {
                        Text(text = stringResource(id = R.string.cancel))
                    }
                    Spacer(modifier = Modifier.padding(4.dp))
                    Button(
                        onClick = {
                            if (currLang == selectedLanguage) {
                                requireContext().showToast(getString(R.string.this_language_is_already_selected))
                            } else {
                                val selectedLangCode = getString(
                                    EnumLanguage.values()
                                        .singleOrNull { getString(it.resStringId) == selectedLanguage }?.languageCode
                                        ?: R.string.english_code
                                )
                                viewModelHome.changeSelectedLang(selectedLangCode)
                                LocaleHelper.setLocale(
                                    requireContext(),
                                    selectedLangCode
                                )
                                dialogState.hide()
                                requireActivity().finish()
                                startActivity(Intent(requireActivity(), HomeActivity::class.java))
                            }
                        }) {
                        Text(text = stringResource(id = R.string.change))
                    }
                }
            }
        }
    }

    //share the app with many ways
    private fun shareApp() {
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(
                Intent.EXTRA_TEXT,
                "https://play.google.com/store/apps/details?id=${requireActivity().packageName}"
            )
        }
        if (intent.resolveActivity(requireContext().packageManager) != null) {
            startActivity(Intent.createChooser(intent, getString(R.string.share_with)))
        } else {
            requireContext().showToast(getString(R.string.you_do_not_have_any_app_execute_this_action))
        }
    }

    private fun rateApp() {
        //Go to the app page on Google play to rate it
        try {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=${requireActivity().packageName}")
                )
            )
        } catch (e: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=${requireActivity().packageName}")
                )
            )
        }
    }

    private fun contactMe() {
        val mailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:amrjyniat@gmail.com")
        }
        if (mailIntent.resolveActivity(requireContext().packageManager) != null) {
            startActivity(Intent.createChooser(mailIntent, getString(R.string.send_mail)))
        } else {
            requireContext().showToast(getString(R.string.you_do_not_have_any_app_execute_this_action))
        }
    }

    private fun setObserversNavigation() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModelHome.homeUiActions.collect { homeAction ->
                    when (homeAction) {
                        is HomeActions.OpenAddTaskAction -> {
                            findNavController().navigate(HomeFragmentDirections.toAddTask())
                        }
                        is HomeActions.OpenTasksListAction -> {
                            findNavController().navigate(HomeFragmentDirections.toTasks(homeAction.taskType))
                        }
                        is HomeActions.OpenTasksDetails -> {
                            findNavController().navigate(
                                HomeFragmentDirections.toTaskDetails(
                                    homeAction.taskId
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    @ExperimentalUnitApi
    @Preview(showBackground = true)
    @Composable
    fun DefaultPreview() {
        FullMangementComposeTheme {
            // A surface container using the 'background' color from the theme
            Surface(color = MaterialTheme.colors.background) {
                Container()
            }
        }
    }


}
