package org.cru.godtools.ui.dashboard.filters

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.ccci.gto.android.common.androidx.compose.material3.ui.button.ButtonWithTrailingIconContentPadding
import org.ccci.gto.android.common.androidx.compose.material3.ui.list.ListItemStartPadding
import org.ccci.gto.android.common.androidx.compose.material3.ui.menu.LazyDropdownMenu
import org.cru.godtools.base.ui.theme.GodToolsTheme
import org.cru.godtools.ui.dashboard.filters.FilterMenu.UiState
import org.cru.godtools.ui.dashboard.filters.FilterMenu.UiState.Item

private val DROPDOWN_MAX_HEIGHT = 700.dp
private val DROPDOWN_MAX_WIDTH = 350.dp

object FilterMenu {
    data class UiState<T>(
        val menuExpanded: MutableState<Boolean> = mutableStateOf(false),
        val query: MutableState<String> = mutableStateOf(""),
        val items: ImmutableList<Item<T>> = persistentListOf(),
        val selectedItem: T? = null,
        val eventSink: (Event<T>) -> Unit = {},
    ) : CircuitUiState {
        data class Item<T>(val item: T, val count: Int)
    }

    sealed interface Event<T> : CircuitUiEvent {
        data class SelectItem<T>(val item: T) : Event<T>
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
fun <T> LazyFilterMenu(
    state: UiState<T>,
    buttonLabelText: String,
    itemLabel: @Composable (item: Item<T>) -> Unit,
    modifier: Modifier = Modifier,
    searchPlaceholder: @Composable (() -> Unit)? = null,
    itemKey: ((item: Item<T>) -> Any)? = null,
    itemSupportingText: @Composable (item: Item<T>) -> String? = { null },
) {
    val eventSink by rememberUpdatedState(state.eventSink)

    var expanded by state.menuExpanded
    var query by state.query

    ElevatedButton(
        onClick = {
            if (!expanded) query = ""
            expanded = true
        },
        contentPadding = ButtonDefaults.ButtonWithTrailingIconContentPadding,
        modifier = modifier.semantics { role = Role.DropdownList }
    ) {
        Text(
            buttonLabelText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)

        LazyDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
                query = ""
            },
            modifier = Modifier.sizeIn(maxHeight = DROPDOWN_MAX_HEIGHT, maxWidth = DROPDOWN_MAX_WIDTH)
        ) {
            stickyHeader {
                Surface(color = MenuDefaults.containerColor) {
                    SearchBar(
                        inputField = {
                            SearchBarDefaults.InputField(
                                query = query,
                                onQueryChange = { query = it },
                                onSearch = { query = it },
                                expanded = false,
                                onExpandedChange = {},
                                placeholder = searchPlaceholder,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        },
                        expanded = false,
                        onExpandedChange = {},
                        colors = GodToolsTheme.searchBarColors,
                        content = {},
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .fillMaxWidth()
                            .wrapContentWidth()
                    )
                }
            }

            itemsIndexed(state.items, key = itemKey?.let { key -> { _, it -> key(it) } }) { index, it ->
                if (index > 0) {
                    HorizontalDivider(Modifier.padding(start = ListItemStartPadding, end = ListItemStartPadding))
                }
                FilterMenuItem(
                    label = { itemLabel(it) },
                    supportingText = itemSupportingText(it),
                    onClick = {
                        eventSink(FilterMenu.Event.SelectItem(it.item))
                        expanded = false
                        query = ""
                    },
                    modifier = Modifier.animateItem()
                )
            }
        }
    }
}
