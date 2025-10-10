package org.cru.godtools.tool.lesson.ui.resume

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.slack.circuit.overlay.Overlay
import com.slack.circuit.overlay.OverlayNavigator
import org.cru.godtools.tool.lesson.R

class LessonResumeDialogOverlay(private val pageId: String) : Overlay<LessonResumeDialogOverlay.Result> {
    @Composable
    override fun Content(navigator: OverlayNavigator<Result>) {
        AlertDialog(
            title = { Text(stringResource(R.string.lesson_resume_title)) },
            text = { Text(stringResource(R.string.lesson_resume_message)) },
            confirmButton = {
                TextButton(onClick = { navigator.finish(Result.Resume(pageId)) }) {
                    Text(stringResource(R.string.lesson_resume_action_resume))
                }
            },
            dismissButton = {
                TextButton(onClick = { navigator.finish(Result.Restart) }) {
                    Text(stringResource(R.string.lesson_resume_action_restart))
                }
            },
            onDismissRequest = { navigator.finish(Result.Restart) },
        )
    }

    sealed interface Result {
        data class Resume(val pageId: String) : Result
        data object Restart : Result
    }
}
