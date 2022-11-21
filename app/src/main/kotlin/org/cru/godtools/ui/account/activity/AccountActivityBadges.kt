package org.cru.godtools.ui.account.activity

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.SizeMode
import org.ccci.gto.android.common.androidx.compose.foundation.text.minLinesHeight
import org.ccci.gto.android.common.androidx.compose.material3.isLight
import org.cru.godtools.R
import org.cru.godtools.base.ui.theme.DisabledAlpha
import org.cru.godtools.shared.user.activity.model.Badge
import org.cru.godtools.shared.user.activity.model.Badge.BadgeType
import org.cru.godtools.shared.user.activity.model.UserActivity
import palettes.TonalPalette

@Composable
internal fun AccountActivityBadges(
    activity: UserActivity,
    modifier: Modifier = Modifier
) = Column(modifier = modifier) {
    Text(
        stringResource(R.string.account_activity_badges_header),
        style = MaterialTheme.typography.titleLarge,
    )

    FlowRow(
        mainAxisSize = SizeMode.Wrap,
        mainAxisSpacing = 16.dp,
        crossAxisSpacing = 16.dp,
        modifier = Modifier
            .padding(top = 16.dp)
            .fillMaxWidth()
            .wrapContentWidth()
    ) {
        activity.badges.forEach { ActivityBadge(badge = it) }
    }
}

@Composable
@OptIn(ExperimentalTextApi::class)
private fun ActivityBadge(badge: Badge, modifier: Modifier = Modifier) = ElevatedCard(
    elevation = CardDefaults.elevatedCardElevation(defaultElevation = if (badge.isEarned) 4.dp else 0.dp),
    modifier = modifier.size(100.dp)
) {
    Spacer(modifier = Modifier.weight(1f))
    BadgeImage(
        badge,
        modifier = Modifier
            .align(Alignment.CenterHorizontally)
            .padding(top = 8.dp)
    )
    Text(
        badge.label,
        style = MaterialTheme.typography.labelSmall.merge(ParagraphStyle(lineBreak = LineBreak.Heading)),
        maxLines = 2,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .minLinesHeight(2, MaterialTheme.typography.labelSmall)
            .alpha(if (badge.isEarned) 1f else DisabledAlpha)
    )
    Spacer(modifier = Modifier.weight(1f))
}

@Composable
private fun BadgeImage(badge: Badge, modifier: Modifier, enabled: Boolean = badge.isEarned) {
    val baseColor = remember(badge.type, enabled) {
        when {
            !enabled -> Color(0xffd5d5d5)
            else -> when (badge.type) {
                BadgeType.TOOLS_OPENED -> Color(0xFF05699B)
                BadgeType.LESSONS_COMPLETED -> Color(0xFFA6EDE8)
                BadgeType.ARTICLES_OPENED -> Color(0xff6750a4)
                BadgeType.IMAGES_SHARED -> Color(0xFF2F3676)
                BadgeType.TIPS_COMPLETED -> Color(0xffE53660)
            }
        }
    }
    val palette = remember(baseColor) { TonalPalette.fromInt(baseColor.toArgb()) }
    val (color, containerColor) = when {
        MaterialTheme.colorScheme.isLight -> Pair(Color(palette.tone(40)), Color(palette.tone(90)))
        else -> Pair(Color(palette.tone(80)), Color(palette.tone(30)))
    }

    Image(
        badge.icon,
        contentDescription = badge.label,
        colorFilter = ColorFilter.tint(color.copy(alpha = if (!enabled) 0.38f else 1f)),
        modifier = modifier
            .size(36.dp)
            .background(containerColor.copy(alpha = if (!enabled) 0.38f else 1f), CircleShape)
    )
}

private val Badge.icon
    @Composable
    get() = when (type) {
        BadgeType.TOOLS_OPENED -> painterResource(R.drawable.ic_badge_trophy)
        BadgeType.LESSONS_COMPLETED -> painterResource(R.drawable.ic_badge_medal)
        BadgeType.ARTICLES_OPENED -> painterResource(R.drawable.ic_badge_trophy)
        BadgeType.IMAGES_SHARED -> painterResource(R.drawable.ic_badge_medal)
        BadgeType.TIPS_COMPLETED -> painterResource(R.drawable.ic_badge_flag)
    }

@OptIn(ExperimentalComposeUiApi::class)
private val Badge.label
    @Composable
    get() = when (type) {
        BadgeType.TOOLS_OPENED -> pluralStringResource(R.plurals.account_activity_badge_tools_opened, target, target)
        BadgeType.LESSONS_COMPLETED -> pluralStringResource(
            R.plurals.account_activity_badge_lessons_completed,
            target,
            target
        )
        BadgeType.ARTICLES_OPENED -> pluralStringResource(
            R.plurals.account_activity_badge_articles_opened,
            target,
            target
        )
        BadgeType.IMAGES_SHARED -> pluralStringResource(R.plurals.account_activity_badge_images_shared, target, target)
        BadgeType.TIPS_COMPLETED -> pluralStringResource(
            R.plurals.account_activity_badge_tips_completed,
            target,
            target
        )
    }
