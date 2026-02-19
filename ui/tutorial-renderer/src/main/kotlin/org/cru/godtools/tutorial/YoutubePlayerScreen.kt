package org.cru.godtools.tutorial

import com.slack.circuitx.android.AndroidScreen
import kotlinx.parcelize.Parcelize

@Parcelize
data class YoutubePlayerScreen(val videoId: String) : AndroidScreen
