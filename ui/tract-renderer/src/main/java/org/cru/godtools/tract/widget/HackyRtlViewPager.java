package org.cru.godtools.tract.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.duolingo.open.rtlviewpager.RtlViewPager;

import org.ccci.gto.android.common.viewpager.util.ViewPagerUtils;

public class HackyRtlViewPager extends RtlViewPager {
    public HackyRtlViewPager(final Context context) {
        super(context);
    }

    public HackyRtlViewPager(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(final MotionEvent ev) {
        try {
            return super.onInterceptTouchEvent(ev);
        } catch (final RuntimeException e) {
            return ViewPagerUtils.handleOnInterceptTouchEventException(e);
        }
    }
}
