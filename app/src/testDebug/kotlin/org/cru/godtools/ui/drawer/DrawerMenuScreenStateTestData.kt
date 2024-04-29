package org.cru.godtools.ui.drawer

import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import org.cru.godtools.ui.drawer.DrawerMenuScreen.State

object DrawerMenuScreenStateTestData {
    val open = State(
        drawerState = DrawerState(DrawerValue.Open),
        versionName = "Paparazzi Snapshot",
        versionCode = 1
    )

    val closed = State(
        drawerState = DrawerState(DrawerValue.Closed),
        versionName = "Paparazzi Snapshot",
        versionCode = 1
    )
}
