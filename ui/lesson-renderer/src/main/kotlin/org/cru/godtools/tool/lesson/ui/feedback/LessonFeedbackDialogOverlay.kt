package org.cru.godtools.tool.lesson.ui.feedback

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.slack.circuit.overlay.Overlay
import com.slack.circuit.overlay.OverlayNavigator
import org.cru.godtools.tool.lesson.R

class LessonFeedbackDialogOverlay(private val pageReached: Int) : Overlay<LessonFeedbackDialogOverlay.Result> {
    sealed interface Result {
        data class Submit(val pageReached: Int, val helpful: Boolean?, val readiness: Int) : Result
        data object Skip : Result
    }

    @Composable
    override fun Content(navigator: OverlayNavigator<Result>) {
        var helpful by rememberSaveable { mutableStateOf<Boolean?>(null) }
        var readiness by rememberSaveable { mutableFloatStateOf(1f) }

        AlertDialog(
            title = { Text(stringResource(R.string.lesson_feedback_title)) },
            text = {
                Column {
                    Text(stringResource(R.string.lesson_feedback_question_helpful))
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        SegmentedButton(
                            selected = helpful == true,
                            onClick = { helpful = if (helpful == true) null else true },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                        ) {
                            Text(stringResource(R.string.lesson_feedback_question_helpful_yes))
                        }
                        SegmentedButton(
                            selected = helpful == false,
                            onClick = { helpful = if (helpful == false) null else false },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                        ) {
                            Text(stringResource(R.string.lesson_feedback_question_helpful_no))
                        }
                    }

                    Text(
                        stringResource(R.string.lesson_feedback_question_readiness),
                        modifier = Modifier.padding(top = 16.dp)
                    )
                    @OptIn(ExperimentalMaterial3Api::class)
                    Slider(
                        value = readiness,
                        onValueChange = { readiness = it },
                        valueRange = 1f..10f,
                        steps = 8,
                        track = {
                            SliderDefaults.Track(
                                sliderState = it,
                                drawStopIndicator = {},
                                drawTick = { _, _ -> },
                            )
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { navigator.finish(Result.Submit(pageReached, helpful, readiness.toInt())) }) {
                    Text(stringResource(R.string.lesson_feedback_action_submit))
                }
            },
            dismissButton = {
                TextButton(onClick = { navigator.finish(Result.Skip) }) {
                    Text(stringResource(R.string.lesson_feedback_action_cancel))
                }
            },
            onDismissRequest = { navigator.finish(Result.Skip) },
        )
    }
}
