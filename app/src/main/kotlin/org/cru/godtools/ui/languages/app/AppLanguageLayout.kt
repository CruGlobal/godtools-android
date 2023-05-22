package org.cru.godtools.ui.languages.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Divider
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun AppLanguageLayout(
    viewModel: AppLanguageViewModel = viewModel(),
    onEvent: (AppLanguageEvent) -> Unit = {},
) {
    val languages by viewModel.languages.collectAsState(emptyList())

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ListItemDefaults.containerColor)
    ) {
        itemsIndexed(languages, { _, it -> it }) { i, lang ->
            ListItem(
                headlineContent = {
                    Row {
                        Text(lang.getDisplayName(lang))

                        val localizedColor = LocalContentColor.current.let { it.copy(alpha = it.alpha * 0.60f) }
                        CompositionLocalProvider(LocalContentColor provides localizedColor) {
                            Text(lang.displayName, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                },
                modifier = Modifier.clickable { onEvent(AppLanguageEvent.LanguageSelected(lang)) }
            )
            if (i != languages.lastIndex) Divider(Modifier.padding(horizontal = 16.dp))
        }
    }
}
