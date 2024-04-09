package org.cru.godtools.ui.drawer

import androidx.lifecycle.ViewModelStore
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf

fun testDrawerViewModel() = DrawerViewModel(
    accountManager = mockk {
        every { isAuthenticatedFlow } returns flowOf(false)
    }
)

fun ViewModelStore.putDrawerViewModel(viewModel: DrawerViewModel = testDrawerViewModel()) = put(
    "androidx.lifecycle.ViewModelProvider.DefaultKey:${DrawerViewModel::class.java.canonicalName}",
    viewModel,
)
