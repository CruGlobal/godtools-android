package org.keynote.godtools.android.snuffy.model;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import org.ccci.gto.android.common.util.XmlPullParserUtils;
import org.greenrobot.eventbus.EventBus;
import org.keynote.godtools.android.R;
import org.keynote.godtools.android.model.Followup;
import org.keynote.godtools.renderer.crureader.bo.GPage.Event.GodToolsEvent;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;

import static org.keynote.godtools.android.snuffy.Constants.DEFAULT_BACKGROUND_COLOR;

public class GtFollowupModal extends GtModel {
    public static final String XML_FOLLOWUP_MODAL = "followup-modal";
    public static final String XML_FALLBACK = "fallback";
    private static final String XML_TITLE = "followup-title";
    private static final String XML_BODY = "followup-body";

    private static final String XML_ATTR_FOLLOWUP_ID = "followup-id";
    private static final String XML_ATTR_LISTENERS = "listeners";

    @NonNull
    private final String mId;

    long mFollowupId = Followup.INVALID_ID;
    @NonNull
    private Set<GodToolsEvent.EventID> mListeners = ImmutableSet.of();
    String mTitle;
    String mBody;
    final List<GtInputField> mInputFields = new ArrayList<>();
    @Nullable
    GtButtonPair mButtonPair;
    @NonNull
    private final List<GtThankYou> mThankYous = new ArrayList<>();

    @VisibleForTesting
    GtFollowupModal(@NonNull final GtPage page, @NonNull final String uniqueId) {
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
    public Set<GodToolsEvent.EventID> getListeners() {
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

    @NonNull
    public List<GtThankYou> getThankYous() {
        return ImmutableList.copyOf(mThankYous);
    }

    @Nullable
    @Override
    public ViewHolder render(@NonNull final Context context, @Nullable final ViewGroup parent,
                             final boolean attachToRoot) {
        final LayoutInflater inflater = LayoutInflater.from(context);

        // inflate the raw view
        final ViewHolder holder = new ViewHolder(inflater.inflate(R.layout.gt_followupmodal, parent, false));
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
        /*mListeners = ParserUtils
                .parseEvents(parser.getAttributeValue(null, XML_ATTR_LISTENERS), getManifest().getPackageCode());*/

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
                    mInputFields.add(GtInputField.fromXml(this, parser));
                    break;
                case GtButtonPair.XML_BUTTON_PAIR:
                    mButtonPair = GtButtonPair.fromXml(this, parser);
                    break;
                case GtThankYou.XML_THANK_YOU:
                    mThankYous.add(GtThankYou.fromXml(this, Integer.toString(mThankYous.size() + 1), parser));
                    break;
                default:
                    // skip unrecognized nodes
                    XmlPullParserUtils.skipTag(parser);
            }
        }
    }

    public class ViewHolder extends GtModel.ViewHolder {
        @Nullable
        @BindView(R.id.title)
        TextView mTitleText;
        @Nullable
        @BindView(R.id.body)
        TextView mBodyText;

        @Nullable
        @BindView(R.id.fields)
        ViewGroup mFields;

        @Nullable
        @BindView(R.id.buttons)
        ViewGroup mButtons;

        private final List<GtInputField.ViewHolder> mFieldViewHolders = new ArrayList<>();

        ViewHolder(@NonNull final View root) {
            super(root);
            ButterKnife.bind(this, mRoot);

            updateBackground();
            updateTitle();
            updateBody();
            attachFields();
            attachButtonPair();
        }

        /* BEGIN lifecycle */

        @Override
        protected boolean onValidate(final boolean validateParent) {
            boolean valid = super.onValidate(validateParent);

            for (final GtInputField.ViewHolder holder : mFieldViewHolders) {
                valid = holder.onValidate(false) && valid;
            }

            return valid;
        }

        @Override
        protected boolean onSendEvent(@NonNull final GodToolsEvent.EventID eventId) {
            if (eventId.equals(GodToolsEvent.EventID.SUBSCRIBE_EVENT)) {
                // build subscribe event object
                final GodToolsEvent event = new GodToolsEvent(eventId);
                event.setPackageCode(getManifest().getPackageCode());
                event.setLanguage(getManifest().getLanguage());
                event.setFollowUpId(mFollowupId);

                // set all input fields as data
                if (mFields != null) {
                    for (final GtInputField.ViewHolder holder : mFieldViewHolders) {
                        if (holder.getName() != null) {
                            event.setField(holder.getName(), holder.getValue());
                        }
                    }
                }

                // send subscribe event
                EventBus.getDefault().post(event);
                return true;
            }

            return super.onSendEvent(eventId);
        }

        /* END lifecycle */

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

        private void attachFields() {
            if (mFields != null) {
                mFields.setVisibility(mInputFields.size() > 0 ? View.VISIBLE : View.GONE);
                for (final GtInputField field : mInputFields) {
                    mFieldViewHolders.add(field.render(mFields.getContext(), mFields, true));
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
