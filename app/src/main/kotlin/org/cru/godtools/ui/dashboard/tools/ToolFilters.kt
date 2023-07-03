package org.cru.godtools.ui.dashboard.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.cru.godtools.R
import org.cru.godtools.base.ui.util.getToolCategoryName

private val POPUP_MAX_HEIGHT = 600.dp

@Composable
internal fun ToolFilters(
    modifier: Modifier = Modifier,
    viewModel: ToolsViewModel = viewModel(),
) = Column(modifier = modifier.fillMaxWidth()) {
    Text(
        stringResource(R.string.dashboard_tools_section_filter_label),
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        CategoryFilter(viewModel, modifier = Modifier.weight(1f))
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun CategoryFilter(viewModel: ToolsViewModel, modifier: Modifier = Modifier) {
    val categories by viewModel.categories.collectAsState()
    var expanded by rememberSaveable { mutableStateOf(false) }

    ElevatedButton(
        onClick = { expanded = !expanded },
        modifier = modifier
    ) {
        val category by viewModel.filterCategory.collectAsState()
        Text(
            category?.let { getToolCategoryName(it, LocalContext.current) }
                ?: stringResource(R.string.dashboard_tools_section_filter_category_any),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = POPUP_MAX_HEIGHT),
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.dashboard_tools_section_filter_category_any)) },
                onClick = {
                    viewModel.setFilterCategory(null)
                    expanded = false
                }
            )
            categories.forEach {
                DropdownMenuItem(
                    text = { Text(getToolCategoryName(it, LocalContext.current)) },
                    onClick = {
                        viewModel.setFilterCategory(it)
                        expanded = false
                    }
                )
            }
        }
    }
}
