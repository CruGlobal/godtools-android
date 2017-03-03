package org.keynote.godtools.android.newnew.appcompat;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

/**
 * Created by rmatt on 2/21/2017.
 */

public class VectorTextView extends AppCompatTextView {

    public VectorTextView(Context context) {
        super(context);
    }

    public VectorTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttrs(context, attrs);
    }

    public VectorTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs);
    }

    void initAttrs(Context context, AttributeSet attrs) {
        VectorCompat.renderSVGAndTint(this, context, attrs);
    }
}
