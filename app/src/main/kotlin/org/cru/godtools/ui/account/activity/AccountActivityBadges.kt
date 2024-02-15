package org.cru.godtools.ui.account.activity

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.ccci.gto.android.common.androidx.compose.foundation.text.minLinesHeight
import org.ccci.gto.android.common.androidx.compose.material3.DisabledAlpha
import org.ccci.gto.android.common.androidx.compose.material3.isLight
import org.ccci.gto.android.common.androidx.compose.ui.text.computeHeightForDefaultText
import org.cru.godtools.R
import org.cru.godtools.shared.common.model.ThemeType
import org.cru.godtools.shared.user.activity.model.Badge
import org.cru.godtools.shared.user.activity.model.Badge.BadgeType
import org.cru.godtools.shared.user.activity.model.UserActivity

private val BADGE_SIZE = 48.dp

@Composable
@OptIn(ExperimentalLayoutApi::class)
internal fun AccountActivityBadges(activity: UserActivity, modifier: Modifier = Modifier) = Column(modifier) {
    Text(
        stringResource(R.string.account_activity_badges_header),
        style = MaterialTheme.typography.titleLarge,
    )

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .padding(top = 16.dp)
            .fillMaxWidth()
            .wrapContentWidth()
    ) {
        activity.badges.forEach { ActivityBadge(badge = it) }
    }
}

@Composable
private fun ActivityBadge(badge: Badge, modifier: Modifier = Modifier) {
    val textStyle = MaterialTheme.typography.labelSmall.merge(ParagraphStyle(lineBreak = LineBreak.Heading))
    val contentHeight = 8.dp + BADGE_SIZE + 8.dp + computeHeightForDefaultText(textStyle, 2) + 8.dp

    ElevatedCard(
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = if (badge.isEarned) 4.dp else 0.dp),
        modifier = modifier.size(contentHeight.coerceAtLeast(100.dp))
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
            style = textStyle,
            maxLines = 2,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .minLinesHeight(2, textStyle)
                .alpha(if (badge.isEarned) 1f else DisabledAlpha)
        )
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun BadgeImage(badge: Badge, modifier: Modifier = Modifier) {
    val themeType = when {
        MaterialTheme.colorScheme.isLight -> ThemeType.LIGHT
        else -> ThemeType.DARK
    }

    Image(
        badge.icon,
        contentDescription = badge.label,
        colorFilter = ColorFilter.tint(badge.colors.color(themeType)),
        modifier = modifier
            .size(BADGE_SIZE)
            .background(badge.colors.containerColor(themeType), CircleShape)
    )
}

private val Badge.icon
    @Composable
    get() = when (type) {
        BadgeType.TOOLS_OPENED -> when (variant) {
            1 -> painterResource(R.drawable.ic_badge_tools_opened_1)
            2 -> painterResource(R.drawable.ic_badge_tools_opened_2)
            else -> painterResource(R.drawable.ic_badge_tools_opened_3)
        }
        BadgeType.LESSONS_COMPLETED -> when (variant) {
            1 -> painterResource(R.drawable.ic_badge_lesson_completed_1)
            2 -> painterResource(R.drawable.ic_badge_lesson_completed_2)
            else -> painterResource(R.drawable.ic_badge_lesson_completed_3)
        }
        BadgeType.ARTICLES_OPENED -> when (variant) {
            1 -> painterResource(R.drawable.ic_badge_articles_opened_1)
            2 -> painterResource(R.drawable.ic_badge_articles_opened_2)
            else -> painterResource(R.drawable.ic_badge_articles_opened_3)
        }
        BadgeType.IMAGES_SHARED -> when (variant) {
            1 -> painterResource(R.drawable.ic_badge_images_shared_1)
            2 -> painterResource(R.drawable.ic_badge_images_shared_2)
            else -> painterResource(R.drawable.ic_badge_images_shared_3)
        }
        BadgeType.TIPS_COMPLETED -> when (variant) {
            1 -> painterResource(R.drawable.ic_badge_tips_completed_1)
            2 -> painterResource(R.drawable.ic_badge_tips_completed_2)
            else -> painterResource(R.drawable.ic_badge_tips_completed_3)
        }
    }

private val Badge.label
    @Composable
    get() = when (type) {
        BadgeType.TOOLS_OPENED -> pluralStringResource(
            R.plurals.account_activity_badge_tools_opened,
            progressTarget,
            progressTarget
        )
        BadgeType.LESSONS_COMPLETED -> pluralStringResource(
            R.plurals.account_activity_badge_lessons_completed,
            progressTarget,
            progressTarget
        )
        BadgeType.ARTICLES_OPENED -> pluralStringResource(
            R.plurals.account_activity_badge_articles_opened,
            progressTarget,
            progressTarget
        )
        BadgeType.IMAGES_SHARED -> pluralStringResource(
            R.plurals.account_activity_badge_images_shared,
            progressTarget,
            progressTarget
        )
        BadgeType.TIPS_COMPLETED -> pluralStringResource(
            R.plurals.account_activity_badge_tips_completed,
            progressTarget,
            progressTarget
        )
    }
