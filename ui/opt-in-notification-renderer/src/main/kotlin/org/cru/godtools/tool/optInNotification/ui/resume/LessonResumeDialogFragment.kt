package org.cru.godtools.tool.optInNotification.ui.resume

import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.cru.godtools.tool.optInNotification.R

class LessonResumeDialogFragment : DialogFragment() {
    companion object {
        const val RESULT_RESUME = "org.cru.godtools.tool.lesson.ui.resume.LessonResumeDialogFragment_RESULT_RESUME"
        const val RESULT_RESTART = "org.cru.godtools.tool.lesson.ui.resume.LessonResumeDialogFragment_RESULT_RESTART"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?) = MaterialAlertDialogBuilder(requireContext())
        .setTitle(R.string.lesson_resume_title)
        .setMessage(R.string.lesson_resume_message)
        .setPositiveButton(R.string.lesson_resume_action_resume) { _, _ ->
            setFragmentResult(RESULT_RESUME, Bundle.EMPTY)
        }
        .setNegativeButton(R.string.lesson_resume_action_restart) { _, _ ->
            setFragmentResult(RESULT_RESTART, Bundle.EMPTY)
        }
        .create()

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        setFragmentResult(RESULT_RESTART, Bundle.EMPTY)
    }
}
