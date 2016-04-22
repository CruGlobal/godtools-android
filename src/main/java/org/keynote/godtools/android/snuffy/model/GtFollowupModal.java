package org.keynote.godtools.android.snuffy.model;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import org.ccci.gto.android.common.util.XmlPullParserUtils;
import org.keynote.godtools.android.R;
import org.keynote.godtools.android.event.GodToolsEvent.EventID;
import org.keynote.godtools.android.model.Followup;
import org.keynote.godtools.android.snuffy.ParserUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import butterknife.Bind;
import butterknife.ButterKnife;

import static org.keynote.godtools.android.snuffy.Constants.DEFAULT_BACKGROUND_COLOR;

public class GtFollowupModal extends GtModel {
    static final String XML_FOLLOWUP_MODAL = "followup-modal";
    private static final String XML_FALLBACK = "fallback";
    private static final String XML_TITLE = "followup-title";
    private static final String XML_BODY = "followup-body";

    private static final String XML_ATTR_FOLLOWUP_ID = "followup-id";
    private static final String XML_ATTR_LISTENERS = "listeners";

    @NonNull
    private final String mId;

    private long mFollowupId = Followup.INVALID_ID;
    @NonNull
    private Set<EventID> mListeners = ImmutableSet.of();
    String mTitle;
    String mBody;
    private final List<GtInputField> mInputFields = new ArrayList<>();
    @Nullable
    GtButtonPair mButtonPair;

    private GtFollowupModal(@NonNull final GtPage page, @NonNull final String uniqueId) {
        super(page);
        mId = page.getId() + "-followup-" + uniqueId;
    }

    @NonNull
    public String getId() {
        return mId;
    }

    public long getFollowupId() {
        return mFollowupId;
    }

    @NonNull
    public Set<EventID> getListeners() {
        return mListeners;
    }

    @ColorInt
    @Nullable
    Integer getBackgroundColor() {
        final GtPage page = getPage();
        if (page != null) {
            return page.getBackgroundColor();
        }
        return null;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getBody() {
        return mBody;
    }

    public List<GtInputField> getInputFields() {
        return ImmutableList.copyOf(mInputFields);
    }

    @Nullable
    public GtButtonPair getButtonPair() {
        return mButtonPair;
    }

    @Nullable
    @Override
    public ViewHolder render(@NonNull final Context context, @Nullable final ViewGroup parent,
                             final boolean attachToRoot) {
        final LayoutInflater inflater = LayoutInflater.from(context);

        // inflate the raw view
        final ViewHolder holder = new ViewHolder(inflater.inflate(R.layout.gt_followup_modal, parent, false));
        if (parent != null && attachToRoot) {
            parent.addView(holder.mRoot);
        }

        return holder;
    }

    @NonNull
    static GtFollowupModal fromXml(@NonNull final GtPage page, @NonNull final String uniqueId,
                                   @NonNull final XmlPullParser parser)
            throws IOException, XmlPullParserException {
        final GtFollowupModal followup = new GtFollowupModal(page, uniqueId);
        followup.parse(parser);
        return followup;
    }

    private void parse(@NonNull final XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, XML_FOLLOWUP_MODAL);

        try {
            mFollowupId = Long.parseLong(parser.getAttributeValue(null, XML_ATTR_FOLLOWUP_ID));
        } catch (final Exception suppressed) {
            mFollowupId = Followup.INVALID_ID;
        }
        mListeners = ParserUtils
                .parseEvents(parser.getAttributeValue(null, XML_ATTR_LISTENERS), getManifest().getPackageCode());

        // loop until we reach the matching end tag for this element
        while (parser.next() != XmlPullParser.END_TAG) {
            // skip anything that isn't a start tag for an element
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            // process recognized elements
            switch (parser.getName()) {
                case XML_FALLBACK:
                    parseFallback(parser);
                    break;
                default:
                    // skip unrecognized nodes
                    XmlPullParserUtils.skipTag(parser);
            }
        }
    }

    private void parseFallback(@NonNull final XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, XML_FALLBACK);

        // loop until we reach the matching end tag for this element
        while (parser.next() != XmlPullParser.END_TAG) {
            // skip anything that isn't a start tag for an element
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            // process recognized elements
            switch (parser.getName()) {
                case XML_TITLE:
                    mTitle = XmlPullParserUtils.safeNextText(parser);
                    break;
                case XML_BODY:
                    mBody = XmlPullParserUtils.safeNextText(parser);
                    break;
                case GtInputField.XML_INPUT_FIELD:
                    mInputFields.add(GtInputField.fromXml(parser));
                    break;
                case GtButtonPair.XML_BUTTON_PAIR:
                    mButtonPair = GtButtonPair.fromXml(this, parser);
                    break;
                default:
                    // skip unrecognized nodes
                    XmlPullParserUtils.skipTag(parser);
            }
        }
    }

    public class ViewHolder extends GtModel.ViewHolder {
        @Nullable
        @Bind(R.id.title)
        TextView mTitleText;
        @Nullable
        TextView mBodyText;

        @Nullable
        @Bind(R.id.buttons)
        ViewGroup mButtons;

        ViewHolder(@NonNull final View root) {
            super(root);
            ButterKnife.bind(this, mRoot);

            updateBackground();
            updateTitle();
            updateBody();
            attachButtonPair();
        }

        private void updateBackground() {
            // update the background color & watermark
            final Integer color = getBackgroundColor();
            mRoot.setBackgroundColor(color != null ? color : DEFAULT_BACKGROUND_COLOR);
        }

        private void updateTitle() {
            if (mTitleText != null) {
                mTitleText.setVisibility(mTitle != null ? View.VISIBLE : View.GONE);
                if (mTitle != null) {
                    mTitleText.setText(mTitle);
                }
            }
        }

        private void updateBody() {
            if (mBodyText != null) {
                mBodyText.setVisibility(mBody != null ? View.VISIBLE : View.GONE);
                if (mBody != null) {
                    mBodyText.setText(mBody);
                }
            }
        }

        private void attachButtonPair() {
            if (mButtons != null) {
                mButtons.setVisibility(mButtonPair != null ? View.VISIBLE : View.GONE);
                if (mButtonPair != null) {
                    final GtModel.ViewHolder holder = mButtonPair.render(mButtons.getContext(), mButtons, true);
                    holder.setParentHolder(this);
                }
            }
        }
    }
}
