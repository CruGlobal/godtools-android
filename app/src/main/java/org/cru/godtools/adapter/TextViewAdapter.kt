package org.cru.godtools.adapter

import android.net.Uri
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import android.util.Patterns
import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter
import org.cru.godtools.base.tool.analytics.model.ExitLinkActionEvent
import org.greenrobot.eventbus.EventBus

@BindingAdapter("webLinkText")
fun TextView.setTextViewWebLinksForAnalytics(
    changedText: CharSequence
) {
    val spannable = SpannableString(changedText)
    val matcher = Patterns.WEB_URL.matcher(spannable)
    while (matcher.find()) {
        var url = spannable.toString().substring(matcher.start(), matcher.end())
        if (!url.startsWith("http")) {
            url = "http://$url"
        }
        val urlSpan = object : URLSpan(url) {
            override fun onClick(widget: View) {
                val uri = Uri.parse(url)
                EventBus.getDefault().post(ExitLinkActionEvent(uri))
                super.onClick(widget)
            }
        }
        spannable.setSpan(urlSpan, matcher.start(), matcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
    text = spannable
    movementMethod = LinkMovementMethod.getInstance() // Make link clickable
}
