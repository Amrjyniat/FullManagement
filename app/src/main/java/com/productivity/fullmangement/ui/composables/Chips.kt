import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview(showBackground = true)
@Composable
fun ChipGroupMultiSelection(
    modifier: Modifier = Modifier,
    items: List<String> = listOf(),
    selectedItems: List<String> = listOf(),
    onSelectedChanged: (String) -> Unit = {},
) {
    Column(modifier = modifier) {
        LazyRow {
            items(items) { item ->
                Chip(
                    name = item,
                    isSelected = selectedItems.contains(item),
                    onSelectionChanged = {
                        onSelectedChanged(it)
                    },
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChipGroupSingleSelection(
    modifier: Modifier = Modifier,
    cars: List<String> = listOf(),
    selectedCar: String = "Test",
    onSelectedChanged: (String) -> Unit = {},
) {
    Column(modifier = modifier) {
        LazyRow {
            items(cars) { item ->
                Chip(
                    name = item,
                    isSelected = selectedCar == item,
                    onSelectionChanged = {
                        onSelectedChanged(it)
                    },
                ){
                    Text(
                        text = item,
                        color = Color.White,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}




@Preview(showBackground = true)
@Composable
fun Chip(
    modifier: Modifier = Modifier,
    onSelectionChanged: (String) -> Unit = {},
    name: String = "Chip",
    isSelected: Boolean = true,
    content: @Composable () -> Unit = {Text(
        text = name,
        color = if (isSelected)  Color.White else Color.Black,
        modifier = Modifier.padding(8.dp)
    )}
) {
    Surface(
        modifier = modifier.padding(4.dp),
        elevation = 8.dp,
        shape = MaterialTheme.shapes.medium,
        color = if (isSelected) MaterialTheme.colors.primary else Color.LightGray
    ) {
        Row(modifier = Modifier
            .toggleable(
                value = isSelected,
                onValueChange = {
                    onSelectionChanged(name)
                }
            )
        ) {
            content()
        }
    }
}