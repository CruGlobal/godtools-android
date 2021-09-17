package org.cru.godtools.tool.lesson.ui.feedback

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import org.ccci.gto.android.common.androidx.fragment.app.DataBindingDialogFragment
import org.cru.godtools.tool.lesson.R
import org.cru.godtools.tool.lesson.databinding.LessonFeedbackDialogBinding

@AndroidEntryPoint
class LessonFeedbackDialogFragment :
    DataBindingDialogFragment<LessonFeedbackDialogBinding>(R.layout.lesson_feedback_dialog) {
    companion object {
        const val RESULT_DISMISSED = "org.cru.godtools.tool.lesson.ui.feedback.LessonFeedbackDialogFragment_DISMISSED"
    }

    private val viewModel by viewModels<LessonFeedbackViewModel>()

    // region Lifecycle
    override fun onCreateDialog(savedInstanceState: Bundle?) = MaterialAlertDialogBuilder(requireContext())
        .setTitle(R.string.lesson_feedback_title)
        .setPositiveButton(R.string.lesson_feedback_action_submit, null)
        .setNegativeButton(R.string.lesson_feedback_action_cancel, null)
        .create()

    override fun onBindingCreated(binding: LessonFeedbackDialogBinding) {
        binding.viewModel = viewModel
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        setFragmentResult(RESULT_DISMISSED, Bundle.EMPTY)
    }
    // endregion Lifecycle
}

class LessonFeedbackViewModel(private val savedState: SavedStateHandle) : ViewModel() {
    val helpful = savedState.getLiveData("helpful", View.NO_ID)
    val readiness = savedState.getLiveData("readiness", 1f)
}
