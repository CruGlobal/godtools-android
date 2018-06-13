package org.cru.godtools.tract.model;

import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.collect.ImmutableSet;

import org.ccci.gto.android.common.util.XmlPullParserUtils;
import org.cru.godtools.base.model.Event;
import org.cru.godtools.tract.R;
import org.cru.godtools.tract.R2;
import org.cru.godtools.tract.util.DrawableUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.Set;

import butterknife.BindView;
import butterknife.OnClick;

import static org.cru.godtools.tract.Constants.XMLNS_CONTENT;
import static org.cru.godtools.tract.Constants.XMLNS_TRACT;
import static org.cru.godtools.tract.model.Text.XML_TEXT;
import static org.cru.godtools.tract.model.Utils.parseColor;

public final class CallToAction extends Base {
    static final String XML_CALL_TO_ACTION = "call-to-action";
    private static final String XML_EVENTS = "events";
    private static final String XML_CONTROL_COLOR = "control-color";

    @Nullable
    Text mLabel;

    @Nullable @ColorInt
    private Integer mControlColor;

    @NonNull
    Set<Event.Id> mEvents = ImmutableSet.of();

    CallToAction(@NonNull final Base parent) {
        super(parent);
    }

    @ColorInt
    static int getControlColor(@Nullable final CallToAction callToAction) {
        return callToAction != null ? callToAction.getControlColor() : Styles.getPrimaryColor(null);
    }

    @ColorInt
    private int getControlColor() {
        return mControlColor != null ? mControlColor : Styles.getPrimaryColor(getPage());
    }

    @NonNull
    static CallToAction fromXml(@NonNull final Base parent, @NonNull final XmlPullParser parser)
            throws IOException, XmlPullParserException {
        return new CallToAction(parent).parse(parser);
    }

    @NonNull
    private CallToAction parse(@NonNull final XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, XMLNS_TRACT, XML_CALL_TO_ACTION);

        mEvents = parseEvents(parser, XML_EVENTS);

        mControlColor = parseColor(parser, XML_CONTROL_COLOR, null);

        // process any child elements
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            // process recognized nodes
            switch (parser.getNamespace()) {
                case XMLNS_CONTENT:
                    switch (parser.getName()) {
                        case XML_TEXT:
                            mLabel = Text.fromXml(this, parser);
                            continue;
                    }
                    break;
            }

            // skip unrecognized nodes
            XmlPullParserUtils.skipTag(parser);
        }

        return this;
    }

    static final class CallToActionViewHolder extends BaseViewHolder<CallToAction> {
        public interface Callbacks {
            void goToNextPage();
        }

        @BindView(R2.id.call_to_action_label)
        TextView mLabelView;
        @BindView(R2.id.call_to_action_arrow)
        ImageView mArrowView;

        @Nullable
        private Callbacks mCallbacks;

        CallToActionViewHolder(@NonNull final View root, @Nullable final BaseViewHolder parentViewHolder) {
            super(CallToAction.class, root, parentViewHolder);
        }

        @NonNull
        public static CallToActionViewHolder forView(@NonNull final View root,
                                                     @Nullable final Page.PageViewHolder parentViewHolder) {
            final CallToActionViewHolder holder = forView(root, CallToActionViewHolder.class);
            return holder != null ? holder : new CallToActionViewHolder(root, parentViewHolder);
        }

        /* BEGIN lifecycle */

        @Override
        void onBind() {
            super.onBind();
            bindLabel();
            bindArrow();
        }

        @OnClick(R2.id.call_to_action_arrow)
        void onTrigger() {
            if (mCallbacks != null && (mModel == null || mModel.mEvents.isEmpty())) {
                mCallbacks.goToNextPage();
            } else if (mModel != null && !mModel.mEvents.isEmpty()) {
                //TODO: trigger events
            }
        }

        /* END lifecycle */

        public void setCallbacks(@Nullable final Callbacks callbacks) {
            mCallbacks = callbacks;
        }

        private void bindLabel() {
            Text.bind(mModel != null ? mModel.mLabel : null, mLabelView);
        }

        private void bindArrow() {
            final boolean visible = mModel == null || !mModel.getPage().isLastPage() || !mModel.mEvents.isEmpty();
            mArrowView.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
            mArrowView.setImageResource(R.drawable.ic_call_to_action);
            mArrowView.setImageDrawable(DrawableUtils.tint(mArrowView.getDrawable(), getControlColor(mModel)));
        }
    }
}
