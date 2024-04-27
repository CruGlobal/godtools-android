package org.cru.godtools.ui.drawer

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.automirrored.outlined.LiveHelp
import androidx.compose.material.icons.automirrored.outlined.Login
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Copyright
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.PersonRemove
import androidx.compose.material.icons.outlined.Policy
import androidx.compose.material.icons.outlined.RateReview
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.booleanResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.play.core.ktx.launchReview
import com.google.android.play.core.ktx.requestReview
import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.coroutines.launch
import org.ccci.gto.android.common.androidx.compose.foundation.layout.padding
import org.ccci.gto.android.common.androidx.compose.material3.ui.navigationdrawer.NavigationDrawerHeadline
import org.cru.godtools.R
import org.cru.godtools.analytics.model.AnalyticsScreenEvent
import org.cru.godtools.base.appLanguage
import org.cru.godtools.base.ui.compose.LocalEventBus
import org.cru.godtools.shared.analytics.AnalyticsActionNames
import org.cru.godtools.shared.analytics.AnalyticsScreenNames
import org.cru.godtools.tutorial.PageSet
import org.cru.godtools.tutorial.startTutorialActivity
import org.cru.godtools.ui.account.delete.startDeleteAccountActivity
import org.cru.godtools.ui.account.startAccountActivity
import org.cru.godtools.ui.drawer.DrawerMenuScreen.Event
import org.cru.godtools.ui.drawer.DrawerMenuScreen.State
import org.cru.godtools.ui.languages.startLanguageSettingsActivity
import org.cru.godtools.ui.login.startLoginActivity

@Composable
fun DrawerMenuLayout(
    modifier: Modifier = Modifier,
    drawerState: DrawerState = rememberDrawerState(DrawerValue.Closed),
    viewModel: DrawerViewModel = viewModel(),
    content: @Composable () -> Unit,
) {
    val scope = rememberCoroutineScope()

    DrawerMenuLayout(
        state = State(
            drawerState = drawerState,
            isLoggedIn = viewModel.isAuthenticatedFlow.collectAsState().value,
            eventSink = {
                when (it) {
                    Event.Logout -> {
                        viewModel.logout()
                        scope.launch { drawerState.close() }
                    }

                    Event.DismissDrawer -> scope.launch { drawerState.close() }
                }
            }
        ),
        modifier = modifier,
        content = content
    )
}

@Composable
fun DrawerMenuLayout(state: State, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val scope = rememberCoroutineScope()
    val drawerState by rememberUpdatedState(state.drawerState)

    BackHandler(enabled = drawerState.isOpen, onBack = { scope.launch { drawerState.close() } })

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = { DrawerContentLayout(state) },
        modifier = modifier,
        content = content,
    )
}

@Composable
private fun DrawerContentLayout(state: State) = ModalDrawerSheet {
    val scope = rememberCoroutineScope()
    val isLoggedIn by rememberUpdatedState(state.isLoggedIn)
    val eventSink by rememberUpdatedState(state.eventSink)

    CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.labelLarge) {
        Column(
            modifier = Modifier
                // HACK: the Material 3 ModalDrawerSheet composable is missing horizontal padding listed in the spec
                .padding(horizontal = 12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            val context = LocalContext.current
            val eventBus = LocalEventBus.current
            val uriHandler = LocalUriHandler.current

            // region Get Started
            NavigationDrawerHeadline(label = { Text(stringResource(R.string.menu_heading_get_started)) })
            if (booleanResource(org.cru.godtools.tutorial.R.bool.show_tutorial_features)) {
                NavigationDrawerItem(
                    icon = { Icon(Icons.Outlined.School, null) },
                    label = { Text(stringResource(R.string.menu_tutorial)) },
                    selected = false,
                    onClick = {
                        context.startTutorialActivity(PageSet.FEATURES)
                        eventSink(Event.DismissDrawer)
                    }
                )
            }
            NavigationDrawerItem(
                icon = { Icon(Icons.Outlined.Translate, null) },
                label = { Text(stringResource(R.string.menu_language_settings)) },
                selected = false,
                onClick = {
                    context.startLanguageSettingsActivity()
                    eventSink(Event.DismissDrawer)
                }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            // endregion Get Started

            // region Account
            if (booleanResource(R.bool.show_login_menu_items)) {
                NavigationDrawerHeadline(label = { Text(stringResource(R.string.menu_heading_account)) })
                if (!isLoggedIn) {
                    NavigationDrawerItem(
                        icon = { Icon(Icons.AutoMirrored.Outlined.Login, null) },
                        label = { Text(stringResource(R.string.menu_login)) },
                        selected = false,
                        onClick = {
                            context.startLoginActivity()
                            eventSink(Event.DismissDrawer)
                        },
                    )
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Outlined.PersonAdd, null) },
                        label = { Text(stringResource(R.string.menu_signup)) },
                        selected = false,
                        onClick = {
                            context.startLoginActivity(createAccount = true)
                            eventSink(Event.DismissDrawer)
                        },
                    )
                } else {
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Outlined.Person, null) },
                        label = { Text(stringResource(R.string.menu_profile)) },
                        selected = false,
                        onClick = {
                            context.startAccountActivity()
                            eventSink(Event.DismissDrawer)
                        }
                    )
                    NavigationDrawerItem(
                        icon = { Icon(Icons.AutoMirrored.Outlined.Logout, null) },
                        label = { Text(stringResource(R.string.menu_logout)) },
                        selected = false,
                        onClick = { eventSink(Event.Logout) }
                    )
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Outlined.PersonRemove, null) },
                        label = { Text(stringResource(R.string.menu_account_delete)) },
                        selected = false,
                        onClick = {
                            context.startDeleteAccountActivity()
                            eventSink(Event.DismissDrawer)
                        },
                    )
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }
            // endregion Account

            // region Support
            NavigationDrawerHeadline(label = { Text(stringResource(R.string.menu_heading_support)) })
            NavigationDrawerItem(
                icon = { Icon(Icons.AutoMirrored.Outlined.Send, null) },
                label = { Text(stringResource(R.string.menu_feedback)) },
                selected = false,
                onClick = {
                    uriHandler.openUri("https://godtoolsapp.com/send-feedback/")
                    eventSink(Event.DismissDrawer)
                }
            )
            NavigationDrawerItem(
                icon = { Icon(Icons.Outlined.BugReport, null) },
                label = { Text(stringResource(R.string.menu_report_bug)) },
                selected = false,
                onClick = {
                    uriHandler.openUri("https://godtoolsapp.com/report-bug/")
                    eventSink(Event.DismissDrawer)
                }
            )
            NavigationDrawerItem(
                icon = { Icon(Icons.AutoMirrored.Outlined.LiveHelp, null) },
                label = { Text(stringResource(R.string.menu_question)) },
                selected = false,
                onClick = {
                    uriHandler.openUri("https://godtoolsapp.com/ask-question/")
                    eventSink(Event.DismissDrawer)
                }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            // endregion Support

            // region Share
            NavigationDrawerHeadline(label = { Text(stringResource(R.string.menu_heading_share)) })
            NavigationDrawerItem(
                icon = { Icon(Icons.Outlined.RateReview, null) },
                label = { Text(stringResource(R.string.menu_review)) },
                selected = false,
                onClick = {
                    scope.launch {
                        val reviewManager = ReviewManagerFactory.create(context)
                        val reviewInfo = reviewManager.requestReview()
                        if (context is Activity) reviewManager.launchReview(context, reviewInfo)
                        eventSink(Event.DismissDrawer)
                    }
                }
            )
            NavigationDrawerItem(
                icon = { Icon(Icons.Outlined.Description, null) },
                label = { Text(stringResource(R.string.menu_share_story)) },
                selected = false,
                onClick = {
                    uriHandler.openUri("https://godtoolsapp.com/share-story/")
                    eventSink(Event.DismissDrawer)
                }
            )
            NavigationDrawerItem(
                icon = { Icon(Icons.Outlined.Share, null) },
                label = { Text(stringResource(R.string.menu_share_godtools)) },
                selected = false,
                onClick = {
                    eventBus.post(
                        AnalyticsScreenEvent(AnalyticsActionNames.PLATFORM_SHARE_GODTOOLS, context.appLanguage)
                    )
                    context.shareGodTools()
                    eventSink(Event.DismissDrawer)
                }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            // endregion Share

            // region About
            NavigationDrawerHeadline(label = { Text(stringResource(R.string.menu_heading_about)) })
            NavigationDrawerItem(
                icon = { Icon(Icons.AutoMirrored.Outlined.FormatListBulleted, null) },
                label = { Text(stringResource(R.string.menu_terms_of_use)) },
                selected = false,
                onClick = {
                    eventBus.post(
                        AnalyticsScreenEvent(AnalyticsScreenNames.PLATFORM_TERMS_OF_USE, context.appLanguage)
                    )
                    uriHandler.openUri("https://godtoolsapp.com/terms-of-use/")
                    eventSink(Event.DismissDrawer)
                }
            )
            NavigationDrawerItem(
                icon = { Icon(Icons.Outlined.Policy, null) },
                label = { Text(stringResource(R.string.menu_privacy_policy)) },
                selected = false,
                onClick = {
                    eventBus.post(
                        AnalyticsScreenEvent(AnalyticsScreenNames.PLATFORM_PRIVACY_POLICY, context.appLanguage)
                    )
                    uriHandler.openUri("https://www.cru.org/about/privacy.html")
                    eventSink(Event.DismissDrawer)
                }
            )
            NavigationDrawerItem(
                icon = { Icon(Icons.Outlined.Copyright, null) },
                label = { Text(stringResource(R.string.menu_copyright)) },
                selected = false,
                onClick = {
                    eventBus.post(
                        AnalyticsScreenEvent(AnalyticsScreenNames.PLATFORM_COPYRIGHT, context.appLanguage)
                    )
                    uriHandler.openUri("https://godtoolsapp.com/copyright/")
                    eventSink(Event.DismissDrawer)
                }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            // endregion About

            NavigationDrawerHeadline(label = {
                Text(stringResource(R.string.menu_version, state.versionName, state.versionCode))
            })
        }
    }
}

private fun Context.shareGodTools() = startActivity(
    Intent.createChooser(
        Intent(Intent.ACTION_SEND)
            .setType("text/plain")
            .putExtra(Intent.EXTRA_SUBJECT, getString(org.cru.godtools.ui.R.string.app_name))
            .putExtra(Intent.EXTRA_TEXT, getString(R.string.share_app_message)),
        null
    )
)
