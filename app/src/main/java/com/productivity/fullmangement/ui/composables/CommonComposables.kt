package com.productivity.fullmangement.ui.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.*
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import com.amr.swipeable_card.Action
import com.amr.swipeable_card.SwipeableCard
import com.productivity.fullmangement.R
import com.productivity.fullmangement.data.domain.TaskDomain
import com.productivity.fullmangement.data.local.EnumRepetition
import com.productivity.fullmangement.data.local.EnumTaskState
import com.productivity.fullmangement.data.local.TaskType
import com.productivity.fullmangement.ui.theme.*
import com.productivity.fullmangement.utils.getActivity
import com.productivity.fullmangement.utils.showDatePicker
import com.productivity.fullmangement.utils.tasksForPreview
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.math.roundToInt


@Composable
fun rightBackIconDirection() = if (LocalLayoutDirection.current == LayoutDirection.Rtl)
    Icons.Default.ArrowForward
else
    Icons.Default.ArrowBack
@Composable
fun rightForwardIconDirection() = if (LocalLayoutDirection.current == LayoutDirection.Rtl)
    Icons.Default.ArrowBack
else
    Icons.Default.ArrowForward

@Composable
fun CustomTopBar(
    title: String,
    buttonIcon: ImageVector? = null,
    onButtonClicked: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(
                text = title
            )
        },
        navigationIcon = {
            if (buttonIcon != null){
                IconButton(onClick = { onButtonClicked() }) {
                    Icon(buttonIcon, contentDescription = "")
                }
            }
        },
        backgroundColor = MaterialTheme.colors.primary,
        actions = { actions() }
    )
}


@Composable
fun CustomFAB(icon: ImageVector, onFABClick: () -> Unit) {
    FloatingActionButton(
        onClick = onFABClick,
        backgroundColor = MaterialTheme.colors.primary,
    ) {
        Icon(
            imageVector = icon,
            tint = Color.White,
            contentDescription = null
        )
    }
}


@ExperimentalUnitApi
@Composable
fun CustomInputText(
    text: String,
    hint: String,
    onTextChange: (String) -> Unit,
    errorResStringId: Int? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = text,
        onValueChange = { newText -> onTextChange(newText) },
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(text = hint, style = textHintStyle) },
        isError = errorResStringId != null,
        colors = TextFieldDefaults.outlinedTextFieldColors(
            errorBorderColor = Color.Red,
            focusedBorderColor = MaterialTheme.colors.secondary,
            unfocusedBorderColor = MaterialTheme.colors.secondary,
        ),
        keyboardOptions = keyboardOptions
    )
}

@ExperimentalAnimationApi
@ExperimentalUnitApi
@Composable
fun CustomInputTextWithTitleAndError(
    textTitle: String,
    hint: String,
    textValue: String,
    onValueChanged: (String) -> Unit,
    errorResStringId: Int? = null,
    modifier: Modifier = Modifier,
) {
    Column {
        CustomTextTitle(textTitle)
        CustomInputText(
            text = textValue,
            hint = hint,
            onTextChange = { newText -> onValueChanged(newText) },
            errorResStringId = errorResStringId,
            modifier = modifier
        )

        AnimatedVisibility(visible = errorResStringId != null) {
            errorResStringId?.let {
                Text(
                    text = stringResource(id = errorResStringId),
                    modifier = Modifier.padding(start = 4.dp),
                    color = Color.Red
                )
            }
        }

    }
}

@Composable
fun CustomInputDateWithTitle(
    textTitle: String,
    value: String,
    onPickerClicked: () -> Unit,
) {
    Column {
        CustomTextTitle(textTitle)
        CustomButtonDate(
            modifier = Modifier.fillMaxWidth(),
            value = value,
            onPickerClicked = onPickerClicked
        )
    }
}

@Composable
fun CustomButtonDate(
    modifier: Modifier = Modifier,
    value: String,
    hint: String = stringResource(R.string.tap_here_to_set_date),
    onPickerClicked: () -> Unit
) {
    Button(
        onClick = onPickerClicked,
        modifier = modifier,
        elevation = ButtonDefaults.elevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(4.dp),
        colors = ButtonDefaults.buttonColors(backgroundColor = Color.White),
        contentPadding = PaddingValues(14.dp),
        border = BorderStroke(
            width = 1.dp,
            color = Teal200
        )
    ) {
        Text(
            text = if (value.isEmpty()) hint else value,
            style = if (value.isEmpty()) textHintStyle else texTitleStyle,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Default.DateRange,
            tint = MaterialTheme.colors.primary,
            contentDescription = null
        )
    }
}


@Composable
fun CustomDropDownMenuWithTitle(
    textTitle: String,
    itemsResId: List<Int>,
    selectedItemResId: Int,
    itemsColor: List<Color> = mutableListOf(),
    selectedColor: Color = Color.Unspecified,
    modifier: Modifier = Modifier,
    onSelectedItem: (Int) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var textFieldSize by remember { mutableStateOf(Size.Zero) }
    val rotateArrow by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f
    )


    Column {
        CustomTextTitle(textTitle)
        Column {
            OutlinedTextField(
                value = stringResource(id = selectedItemResId),
                onValueChange = { onSelectedItem(it.toInt()) },
                modifier = modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .onGloballyPositioned { coordinates ->
                        textFieldSize = coordinates.size.toSize()
                    },
                trailingIcon = {
                    Row {
                        ColoredCircleInRowScope(selectedColor)
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            modifier = Modifier
                                .padding(6.dp)
                                .clickable { expanded = !expanded }
                                .rotate(rotateArrow),
                            contentDescription = "contentDescription"
                        )
                    }
                },
                enabled = false,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    disabledTextColor = LocalContentColor.current.copy(LocalContentAlpha.current),
                    disabledBorderColor = MaterialTheme.colors.secondary
                )
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.width(
                    with(LocalDensity.current) { textFieldSize.width.toDp() })
            ) {
                itemsResId.forEachIndexed { idx, selectedItemResId ->
                    DropdownMenuItem(onClick = {
                        expanded = false
                        onSelectedItem(selectedItemResId)
                    }) {
                        Row {
                            Text(text = stringResource(id = selectedItemResId), Modifier.weight(1f))
                            ColoredCircleInRowScope(itemsColor.getOrNull(idx) ?: Color.Unspecified)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CustomDropDownMenuWithTitle(
    textTitle: String,
    items: List<String>,
    selectedItem: String,
    itemsColor: List<Color> = mutableListOf(),
    selectedColor: Color = Color.Unspecified,
    modifier: Modifier = Modifier,
    onSelectedItem: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var textFieldSize by remember { mutableStateOf(Size.Zero) }
    val rotateArrow by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f
    )

    Column {
        CustomTextTitle(textTitle)
        Column {
            OutlinedTextField(
                value = selectedItem,
                onValueChange = { onSelectedItem(it) },
                modifier = modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .onGloballyPositioned { coordinates ->
                        textFieldSize = coordinates.size.toSize()
                    },
                trailingIcon = {
                    Row {
                        ColoredCircleInRowScope(selectedColor)
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            modifier = Modifier
                                .padding(6.dp)
                                .clickable { expanded = !expanded }
                                .rotate(rotateArrow),
                            contentDescription = "contentDescription"
                        )
                    }
                },
                enabled = false,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    disabledTextColor = LocalContentColor.current.copy(LocalContentAlpha.current),
                    disabledBorderColor = MaterialTheme.colors.secondary
                )
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.width(
                    with(LocalDensity.current) { textFieldSize.width.toDp() })
            ) {
                items.forEachIndexed { idx, selectedItem ->
                    DropdownMenuItem(onClick = {
                        expanded = false
                        onSelectedItem(selectedItem)
                    }) {
                        Row {
                            Text(text = selectedItem, Modifier.weight(1f))
                            ColoredCircleInRowScope(itemsColor.getOrNull(idx) ?: Color.Unspecified)
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun CustomTextTitle(textTitle: String) {
    Text(
        text = textTitle,
        style = texTitleStyle,
        modifier = Modifier.padding(start = 12.dp, bottom = 4.dp, top = 12.dp),
    )
}

@Composable
fun RowScope.ColoredCircleInRowScope(color: Color) {
    ColoredCircle(
        color = color,
        modifier = Modifier.align(Alignment.CenterVertically)
    )
}

@Composable
fun ColoredCircle(color: Color, modifier: Modifier) {
    if (color != Color.Unspecified) {
        Box(
            modifier = modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(color = color)
        )
    }
}


@Composable
fun ItemCardTask(
    task: TaskDomain,
    modifier: Modifier = Modifier,
    onCardClick: (Long) -> Unit
) {
    val constraints = ConstraintSet {
        val title = createRefFor("textTitle")
        val state = createRefFor("textState")
        val repetition = createRefFor("iconRepetition")
        val date = createRefFor("textEndDate")
        val id = createRefFor("textId")
        val priority = createRefFor("iconPriority")

        constrain(title) {
            top.linkTo(parent.top)
            start.linkTo(parent.start)
        }
        constrain(state) {
            top.linkTo(title.bottom)
            start.linkTo(title.start)
        }
        constrain(repetition) {
            top.linkTo(state.bottom)
            bottom.linkTo(parent.bottom)
            start.linkTo(title.start)
        }
        constrain(date) {
            top.linkTo(state.bottom)
            bottom.linkTo(parent.bottom)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }
        constrain(id) {
            bottom.linkTo(parent.bottom)
            end.linkTo(parent.end)
        }
        constrain(priority) {
            top.linkTo(state.top)
            end.linkTo(id.end)
            bottom.linkTo(id.top)
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                onCardClick(task.taskId)
            }
    ) {
        ConstraintLayout(constraints, Modifier.padding(8.dp)) {
            Text(
                text = task.title,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.layoutId("textTitle")
            )
            Text(
                text = stringResource(task.taskState.resIdTaskStateName),
                color = when (task.taskState.taskStateId) {
                    EnumTaskState.IN_PROGRESS.taskStateId -> AllTasksSection
                    EnumTaskState.DONE.taskStateId -> CompletedTasksSection
                    else -> Color.Gray
                },
                modifier = Modifier
                    .layoutId("textState")
                    .padding(top = 4.dp)
            )
            if (task.repetition.repetitionId != EnumRepetition.NOT_REPEATED.repetitionId) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_repetition),
                    modifier = Modifier.layoutId("iconRepetition"),
                    tint = Color.Unspecified,
                    contentDescription = null
                )
            }
            Text(
                text = task.dueDateTime.orEmpty(),
                color = if (task.isExpirationDate) GoogleLightRed else Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.layoutId("textEndDate")
            )
            Text(
                text = task.taskId.toString(),
                modifier = Modifier.layoutId("textId")
            )
            ColoredCircle(
                color = task.priority.color,
                modifier = Modifier.layoutId("iconPriority")
            )
        }
    }
}


@Preview(showBackground = true, backgroundColor = 0x989a82)
@Composable
fun PreviewTaskCard(
    @PreviewParameter(TaskProviderForPreview::class) task: TaskDomain
) {
    ItemCardTask(task = task) {}
}

class TaskProviderForPreview : PreviewParameterProvider<TaskDomain> {
    override val values: Sequence<TaskDomain> = sequenceOf(
        tasksForPreview[0]
    )
}

@Composable
fun ShowSureDeleteTaskDialog(
    onDismissButton: () -> Unit,
    onConfirmButton: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissButton,
        title = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = stringResource(R.string.warning),
                    style = texTitleStyle,
                )
            }
        },
        text = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = stringResource(R.string.are_you_sure_to_delete_the_task),
                )
            }
        },
        confirmButton = {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(onClick = onDismissButton) {
                    Text(text = stringResource(id = R.string.cancel))
                }
                Spacer(modifier = Modifier.padding(6.dp))
                Button(
                    onClick = onConfirmButton,
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = GoogleLightRed,
                        contentColor = Color.White
                    ),

                    ) {
                    Text(text = stringResource(id = R.string.delete))
                }
            }
        },

        )
}

@ExperimentalAnimationApi
@Composable
fun SwipeableCardTask(
    task: TaskDomain,
    taskType: TaskType,
    revealTasksId: State<List<Int>>,
    onClickCard: () -> Unit,
    onArchivedClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onExpand: () -> Unit,
    onCollapse: () -> Unit
) {
    val textArchiveAction = if (taskType == TaskType.ARCHIVED)
        R.string.unarchive
    else
        R.string.archive

    val iconArchiveAction = if (taskType == TaskType.ARCHIVED)
        R.drawable.ic_unarchive
    else
        R.drawable.ic_archive

    SwipeableCard(
        actions = listOf(
            Action(
                iconRes = iconArchiveAction,
                color = AllTasksSection,
                widthInDp = 90.dp,
                text = stringResource(id = textArchiveAction),
                onAction = onArchivedClick
            ),
            Action(
                iconRes = R.drawable.ic_delete,
                color = OutDateTasksSection,
                widthInDp = 90.dp,
                text = stringResource(id = R.string.delete),
                onAction = onDeleteClick
            ),
        ),
        paddingValues = PaddingValues(4.dp),
        isRevealed = revealTasksId.value.contains(task.taskId.toInt()),
        onExpand = onExpand,
        onCollapse = onCollapse) {
        ItemCardTask(task = task) {
            onClickCard()
        }
    }
}

@Composable
fun EmptyList(
    textValue: String,
    content: @Composable ColumnScope.() -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_empty),
            contentDescription = null,
            tint = MaterialTheme.colors.primary
        )
        Spacer(modifier = Modifier.padding(10.dp))
        Text(
            text = textValue,
            fontSize = 20.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.padding(4.dp))
        content()
    }
}


@Composable
fun SelectCustomDate(
    scrollState: ScrollState,
    fromDate: String,
    toDate: String,
    onSelectedFromDate: (Long) -> Unit,
    onSelectedToDate: (Long) -> Unit,
) {
    val activity = LocalContext.current.getActivity()
    val coroutineScope = rememberCoroutineScope()
    var scrollToPosition by remember { mutableStateOf(0F) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(top = 8.dp)
            .onGloballyPositioned { coordinates ->
                scrollToPosition = coordinates.positionInRoot().y
            }
    ) {
        CustomButtonDate(
            modifier = Modifier.weight(1f),
            value = fromDate,
            hint = stringResource(R.string.from),
            onPickerClicked = {
                showDatePicker(activity, false) { dateInMills ->
                    onSelectedFromDate(dateInMills)
                }
            }
        )

        Icon(
            imageVector = rightForwardIconDirection(),
            contentDescription = "To",
            tint = MaterialTheme.colors.primary,
            modifier = Modifier.padding(horizontal = 10.dp)
        )

        CustomButtonDate(
            modifier = Modifier.weight(1f),
            value = toDate,
            hint = stringResource(R.string.to),
            onPickerClicked = {
                showDatePicker(activity, false) { dateInMills ->
                    onSelectedToDate(dateInMills)
                }
            }
        )

        coroutineScope.launch {
            scrollState.animateScrollTo(scrollToPosition.roundToInt())
        }

    }
}

@Composable
fun CheckBoxWithTitle(
    title: String,
    checkedState: Boolean,
    modifier: Modifier = Modifier,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.clickable {
            onCheckedChange(!checkedState)
        },
    ) {
        Checkbox(
            checked = checkedState,
            onCheckedChange = { onCheckedChange(it) }
        )
        Text(text = title)
    }
}
