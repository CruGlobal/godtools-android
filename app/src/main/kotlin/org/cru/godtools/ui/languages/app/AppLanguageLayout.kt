package org.cru.godtools.ui.languages.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.cru.godtools.R
import org.cru.godtools.base.ui.theme.GodToolsAppBarColors

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AppLanguageLayout(
    viewModel: AppLanguageViewModel = viewModel(),
    onEvent: (AppLanguageEvent) -> Unit = {},
) {
    val languages by viewModel.languages.collectAsState(emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { onEvent(AppLanguageEvent.NavigateBack) }) {
                        Icon(Icons.Filled.ArrowBack, null)
                    }
                },
                title = { Text(stringResource(R.string.language_settings_app_language_title)) },
                colors = GodToolsAppBarColors,
            )
        }
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(contentPadding)
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
}
