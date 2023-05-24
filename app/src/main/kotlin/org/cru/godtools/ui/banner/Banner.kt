package org.cru.godtools.ui.banner

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.constrain
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun Banner(
    text: String,
    primaryButton: String,
    modifier: Modifier = Modifier,
    primaryAction: () -> Unit = {},
    secondaryButton: String? = null,
    secondaryAction: () -> Unit = {},
    icon: Painter? = null,
    iconTint: Color = if (icon != null) LocalContentColor.current else Color.Unspecified,
) = Surface(modifier = modifier.fillMaxWidth()) {
    Column {
        CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
            val iconNode = "icon"
            val textNode = "text"
            val primaryActionNode = "primaryAction"
            val secondaryActionNode = "secondaryAction"

            Layout({
                Text(text, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.layoutId(textNode))
                TextButton(
                    onClick = primaryAction,
                    modifier = Modifier
                        .layoutId(primaryActionNode)
                        .heightIn(min = 36.dp)
                ) { Text(primaryButton) }
                if (secondaryButton != null) {
                    TextButton(
                        onClick = secondaryAction,
                        modifier = Modifier
                            .layoutId(secondaryActionNode)
                            .heightIn(min = 36.dp)
                    ) { Text(secondaryButton) }
                }
                if (icon != null) {
                    Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.layoutId(iconNode))
                }
            }) { measurables, constraints ->
                require(constraints.hasBoundedWidth) { "Banner requires a bounded width" }

                val textMargin = 16.dp.roundToPx()
                val actionMargin = 8.dp.roundToPx()
                val iconSize = 40.dp.roundToPx()

                val iconPlaceable = measurables.firstOrNull { it.layoutId == iconNode }
                    ?.measure(constraints.constrain(Constraints.fixed(iconSize, iconSize)))
                val textPlaceable = measurables.first { it.layoutId == textNode }.measure(
                    constraints
                        .offset(horizontal = iconPlaceable?.let { 0 - textMargin - it.width } ?: 0)
                        .offset(horizontal = -2 * textMargin)
                )
                val primaryActionPlaceable = measurables.first { it.layoutId == primaryActionNode }
                    .measure(constraints.offset(horizontal = -2 * actionMargin))
                val secondaryActionPlaceable = measurables.firstOrNull { it.layoutId == secondaryActionNode }
                    ?.measure(constraints.offset(horizontal = -2 * actionMargin))

                val bannerWidth = constraints.maxWidth

                when {
                    // single line layout
                    iconPlaceable == null &&
                        textMargin + textPlaceable.width + 36.dp.roundToPx() +
                        primaryActionPlaceable.width + actionMargin +
                        (secondaryActionPlaceable?.let { it.width + actionMargin } ?: 0) < bannerWidth -> {
                        val bannerHeight = maxOf(
                            textPlaceable.height,
                            primaryActionPlaceable.height,
                            secondaryActionPlaceable?.height ?: 0
                        ) + 10.dp.roundToPx() + actionMargin

                        // calculate placeable positions
                        val centerLine = (bannerHeight + 2.dp.roundToPx()) / 2
                        val primaryActionPosition = IntOffset(
                            bannerWidth - actionMargin - primaryActionPlaceable.width,
                            centerLine - (primaryActionPlaceable.height / 2)
                        )
                        val secondaryActionPosition = IntOffset(
                            primaryActionPosition.x - actionMargin - (secondaryActionPlaceable?.width ?: 0),
                            centerLine - ((secondaryActionPlaceable?.height ?: 0) / 2)
                        )

                        layout(bannerWidth, bannerHeight) {
                            textPlaceable.placeRelative(textMargin, centerLine - (textPlaceable.height / 2))
                            primaryActionPlaceable.placeRelative(primaryActionPosition)
                            secondaryActionPlaceable?.placeRelative(secondaryActionPosition)
                        }
                    }
                    // default layout
                    else -> {
                        val iconPosition = when (iconPlaceable) {
                            null -> IntOffset.Zero
                            else -> IntOffset(textMargin, textMargin)
                        }
                        val textPosition = when (iconPlaceable) {
                            null -> IntOffset(textMargin, textMargin)
                            else -> IntOffset(iconPosition.x + iconPlaceable.width + textMargin, textMargin)
                        }
                        val primaryActionPosition = IntOffset(
                            bannerWidth - actionMargin - primaryActionPlaceable.width,
                            maxOf(
                                iconPlaceable?.let { iconPosition.y + it.height } ?: 0,
                                textPosition.y + textPlaceable.height,
                            ) + 12.dp.roundToPx()
                        )
                        val secondaryActionPosition = when (secondaryActionPlaceable) {
                            null -> IntOffset.Zero
                            else -> {
                                val sameLinePosition = IntOffset(
                                    primaryActionPosition.x - actionMargin - secondaryActionPlaceable.width,
                                    primaryActionPosition.y
                                )
                                val nextLinePosition = IntOffset(
                                    bannerWidth - actionMargin - secondaryActionPlaceable.width,
                                    primaryActionPosition.y + primaryActionPlaceable.height + actionMargin
                                )
                                sameLinePosition.takeUnless { it.x < actionMargin } ?: nextLinePosition
                            }
                        }

                        val bannerHeight = maxOf(
                            iconPlaceable?.let { iconPosition.y + it.height + textMargin } ?: 0,
                            textPosition.y + textPlaceable.height + textMargin,
                            primaryActionPosition.y + primaryActionPlaceable.height + actionMargin,
                            secondaryActionPlaceable?.let { secondaryActionPosition.y + it.height + actionMargin } ?: 0,
                        )

                        layout(bannerWidth, bannerHeight) {
                            iconPlaceable?.placeRelative(iconPosition)
                            textPlaceable.placeRelative(textPosition)
                            primaryActionPlaceable.placeRelative(primaryActionPosition)
                            secondaryActionPlaceable?.placeRelative(secondaryActionPosition)
                        }
                    }
                }
            }
        }
        Divider(modifier = Modifier.alpha(0.12f))
    }
}
