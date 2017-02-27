package godtools.keynote.org.gttestui.appcompat;

import android.content.Context;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;

/**
 * Created by rmatt on 2/21/2017.
 */

public class VectorButton extends AppCompatButton {

    public VectorButton(Context context) {
        super(context);
    }
    public VectorButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttrs(context, attrs);
    }
    public VectorButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs);
    }

    void initAttrs(Context context, AttributeSet attrs) {
        VectorCompat.renderSVGAndTint(this, context, attrs);
    }
}
