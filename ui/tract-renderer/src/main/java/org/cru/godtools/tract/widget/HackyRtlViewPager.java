package org.cru.godtools.tract.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.duolingo.open.rtlviewpager.RtlViewPager;

import org.ccci.gto.android.common.util.view.ViewUtils;

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
            return ViewUtils.handleOnInterceptTouchEventException(e);
        }
    }

    @Override
    public boolean onTouchEvent(final MotionEvent ev) {
        try {
            return super.onTouchEvent(ev);
        } catch (final RuntimeException e) {
            return ViewUtils.handleOnTouchEventException(e);
        }
    }
}
