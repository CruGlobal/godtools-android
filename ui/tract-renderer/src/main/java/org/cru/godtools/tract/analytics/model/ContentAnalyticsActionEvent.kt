package org.cru.godtools.tract.analytics.model

import org.cru.godtools.analytics.model.AnalyticsActionEvent
import org.cru.godtools.analytics.model.AnalyticsSystem
import org.cru.godtools.xml.model.AnalyticsEvent
import timber.log.Timber

const val TAG = "ContentAnalyticsActionEvent"

class ContentAnalyticsActionEvent(private val event: AnalyticsEvent) :
    AnalyticsActionEvent(action = event.action.orEmpty()) {
    override fun isForSystem(system: AnalyticsSystem) = event.isForSystem(system)
    override val adobeAttributes: Map<String, *>? get() = event.attributes
    override val firebaseEventName = if (isForSystem(AnalyticsSystem.ADOBE)) {
        Timber.tag(TAG).e(UnsupportedOperationException("XML Adobe Analytics Usage"), "action ${event.action}")
        when (event.action) {
            "KGP-US Circle Toggled" -> "KGP_US_Circle_Toggled"
            "KGP-US Gospel Presented" -> "KGP_US_Gospel_Presented"
            "KGP-US Not Ready to Decide" -> "KGP_US_Not_Ready_to_Decide"
            "KGP-US New Professing Believer" -> "KGP_US_New_Professing_Believer"
            "KGP-US Already Made Decision" -> "KGP_US_Already_Made_Decision"
            "KGP Circle Toggled" -> "KGP_Circle_Toggled"
            "KGP Gospel Presented" -> "KGP_Gospel_Presented"
            "KGP Not Ready to Decide" -> "KGP_Not_Ready_to_Decide"
            "KGP New Professing Believer" -> "KGP_New_Professing_Believer"
            "KGP Already Made Decision" -> "KGP_Already_Made_Decision"
            "KGP Email Sign Up" -> "KGP_Email_Sign_Up"
            "FourLaws Gospel Presented" -> "FourLaws_Gospel_Presented"
            "FourLaws Not Ready to Decide" -> "FourLaws_Not_Ready_to_Decide"
            "FourLaws New Professing Believer" -> "FourLaws_New_Professing_Believer"
            "FourLaws Already Made Decision" -> "FourLaws_Already_Made_Decision"
            "FourLaws Email Sign Up" -> "FourLaws_Email_Sign_Up"
            "Satisfied Holy Spirit Presented" -> "Satisfied_Holy_Spirit_Presented"
            "HonorRestored Gospel Presented" -> "HonorRestored_Gospel_Presented"
            "TheFour Gospel Presented" -> "TheFour_Gospel_Presented"
            "TheFour Not Ready to Decide" -> "TheFour_Not_Ready_to_Decide"
            "TheFour New Professing Believer" -> "TheFour_New_Professing_Believer"
            "TheFour Already Made Decision" -> "TheFour_Already_Made_Decision"
            "PowerOverFear Gospel Presented" -> "PowerOverFear_Gospel_Presented"
            else -> super.firebaseEventName
        }
    } else {
        super.firebaseEventName
    }
}
