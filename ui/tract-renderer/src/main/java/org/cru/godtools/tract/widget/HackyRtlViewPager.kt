package org.cru.godtools.tract.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import com.duolingo.open.rtlviewpager.RtlViewPager
import org.ccci.gto.android.common.util.view.ViewUtils

class HackyRtlViewPager @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null) :
    RtlViewPager(context, attrs) {
    override fun onInterceptTouchEvent(ev: MotionEvent) = try {
        super.onInterceptTouchEvent(ev)
    } catch (e: RuntimeException) {
        ViewUtils.handleOnInterceptTouchEventException(e)
    }

    override fun onTouchEvent(ev: MotionEvent) = try {
        super.onTouchEvent(ev)
    } catch (e: RuntimeException) {
        ViewUtils.handleOnTouchEventException(e)
    }
}
