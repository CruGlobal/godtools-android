package org.cru.godtools.ui.dashboard.tools

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.ccci.gto.android.common.androidx.compose.foundation.text.minLinesHeight
import org.ccci.gto.android.common.androidx.compose.ui.text.computeHeightForDefaultText
import org.ccci.gto.android.common.util.content.getString
import org.cru.godtools.R
import org.cru.godtools.base.ui.theme.DisabledAlpha
import org.cru.godtools.base.ui.util.getToolCategoryName

private val filterCardLabelStyle: TextStyle
    @Composable get() {
        val baseStyle = MaterialTheme.typography.labelLarge
        return remember(baseStyle) { baseStyle.merge(TextStyle(fontWeight = FontWeight.Bold)) }
    }

@Composable
internal fun ToolFilters(
    viewModel: ToolsViewModel = viewModel(),
    modifier: Modifier = Modifier,
) {
    val categories by viewModel.categories.collectAsState()
    val rows by remember { derivedStateOf { if (categories.size >= 2) 2 else 1 } }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            stringResource(R.string.dashboard_tools_section_categories_label),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        val filterCardHeight = computeHeightForDefaultText(filterCardLabelStyle, 2) + 32.dp
        LazyHorizontalGrid(
            rows = GridCells.Fixed(rows),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .padding(top = 8.dp)
                .height((filterCardHeight * rows) + (8.dp * (rows - 1)))
        ) {
            item("all-tools") { AllToolsFilter(viewModel) }
            items(categories, { "category:$it" }) { CategoryFilter(it, viewModel) }
        }
    }
}

@Composable
private fun AllToolsFilter(viewModel: ToolsViewModel) {
    val filterCategory by viewModel.filterCategory.collectAsState()
    val isSelected by remember { derivedStateOf { filterCategory == null } }

    val context = LocalContext.current
    val primaryLanguage by viewModel.primaryLanguage.collectAsState()
    val label by remember {
        derivedStateOf { context.getString(primaryLanguage, R.string.dashboard_tools_section_categories_all) }
    }

    FilterCard(
        label,
        isSelected = isSelected,
        fadeText = !isSelected,
        onClick = { viewModel.setFilterCategory(null) }
    )
}

@Composable
private fun CategoryFilter(category: String, viewModel: ToolsViewModel) {
    val filterCategory by viewModel.filterCategory.collectAsState()
    val isSelected by remember { derivedStateOf { filterCategory == category } }

    val context = LocalContext.current
    val primaryLanguage by viewModel.primaryLanguage.collectAsState()
    val label by remember { derivedStateOf { getToolCategoryName(category, context, primaryLanguage) } }

    FilterCard(
        label,
        isSelected = isSelected,
        fadeText = !isSelected && filterCategory != null,
        onClick = {
            viewModel.setFilterCategory(category.takeUnless { category == filterCategory })
        }
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun FilterCard(
    label: String,
    isSelected: Boolean,
    fadeText: Boolean = false,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        MaterialTheme.colorScheme.primary.let { if (isSelected) it else it.copy(alpha = 0f) }
    )
    // TODO: change to ElevatedCard(border = ) if border property is ever supported on ElevatedCard
    Card(
        onClick = onClick,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.elevatedCardColors(),
        border = BorderStroke(2.dp, borderColor),
        modifier = Modifier
            .width(with(LocalDensity.current) { 122.sp.toDp() })
    ) {
        val style = filterCardLabelStyle
        val textAlpha by animateFloatAsState(if (fadeText) DisabledAlpha else 1f)

        Text(
            label,
            maxLines = 2,
            style = style,
            modifier = Modifier
                .padding(all = 16.dp)
                .minLinesHeight(2, style)
                .alpha(textAlpha)
        )
    }
}
