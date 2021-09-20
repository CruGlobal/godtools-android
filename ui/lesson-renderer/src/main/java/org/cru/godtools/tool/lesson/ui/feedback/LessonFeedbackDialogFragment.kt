package org.cru.godtools.tool.lesson.ui.feedback

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import javax.inject.Inject
import org.ccci.gto.android.common.androidx.fragment.app.DataBindingDialogFragment
import org.cru.godtools.base.Settings
import org.cru.godtools.tool.lesson.R
import org.cru.godtools.tool.lesson.analytics.model.LessonFeedbackAnalyticsEvent
import org.cru.godtools.tool.lesson.analytics.model.LessonFeedbackAnalyticsEvent.Companion.PARAM_HELPFUL
import org.cru.godtools.tool.lesson.analytics.model.LessonFeedbackAnalyticsEvent.Companion.PARAM_PAGE_REACHED
import org.cru.godtools.tool.lesson.analytics.model.LessonFeedbackAnalyticsEvent.Companion.PARAM_READINESS
import org.cru.godtools.tool.lesson.analytics.model.LessonFeedbackAnalyticsEvent.Companion.VALUE_HELPFUL_NO
import org.cru.godtools.tool.lesson.analytics.model.LessonFeedbackAnalyticsEvent.Companion.VALUE_HELPFUL_YES
import org.cru.godtools.tool.lesson.databinding.LessonFeedbackDialogBinding
import org.greenrobot.eventbus.EventBus
import splitties.fragmentargs.arg

@AndroidEntryPoint
class LessonFeedbackDialogFragment() :
    DataBindingDialogFragment<LessonFeedbackDialogBinding>(R.layout.lesson_feedback_dialog) {
    companion object {
        const val RESULT_DISMISSED = "org.cru.godtools.tool.lesson.ui.feedback.LessonFeedbackDialogFragment_DISMISSED"
    }

    constructor(lesson: String, locale: Locale, pageReached: Int) : this() {
        this.lesson = lesson
        this.locale = locale
        this.pageReached = pageReached
    }

    private var lesson: String by arg()
    private var locale: Locale by arg()
    private var pageReached: Int by arg()

    @Inject
    internal lateinit var eventBus: EventBus
    @Inject
    internal lateinit var settings: Settings
    private val viewModel by viewModels<LessonFeedbackViewModel>()

    // region Lifecycle
    override fun onCreateDialog(savedInstanceState: Bundle?) = MaterialAlertDialogBuilder(requireContext())
        .setTitle(R.string.lesson_feedback_title)
        .setPositiveButton(R.string.lesson_feedback_action_submit) { _, _ -> sendFeedback() }
        .setNegativeButton(R.string.lesson_feedback_action_cancel, null)
        .create()

    override fun onBindingCreated(binding: LessonFeedbackDialogBinding) {
        binding.viewModel = viewModel
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        settings.setFeatureDiscovered(Settings.FEATURE_LESSON_FEEDBACK + lesson)
        setFragmentResult(RESULT_DISMISSED, Bundle.EMPTY)
    }
    // endregion Lifecycle

    private fun sendFeedback() {
        val args = bundleOf(
            PARAM_PAGE_REACHED to pageReached,
            PARAM_HELPFUL to when (viewModel.helpful.value) {
                R.id.yes -> VALUE_HELPFUL_YES
                R.id.no -> VALUE_HELPFUL_NO
                else -> null
            },
            PARAM_READINESS to viewModel.readiness.value?.toInt()
        )
        eventBus.post(LessonFeedbackAnalyticsEvent(lesson, locale, args))
    }
}

class LessonFeedbackViewModel(savedState: SavedStateHandle) : ViewModel() {
    val helpful = savedState.getLiveData("helpful", View.NO_ID)
    val readiness = savedState.getLiveData("readiness", 1f)
}
