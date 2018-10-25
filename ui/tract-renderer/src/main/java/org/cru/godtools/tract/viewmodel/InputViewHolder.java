package org.cru.godtools.tract.viewmodel;

import android.content.res.ColorStateList;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textfield.TextInputLayoutUtils;

import org.cru.godtools.base.model.Event;
import org.cru.godtools.tract.R;
import org.cru.godtools.tract.R2;
import org.cru.godtools.xml.model.Base;
import org.cru.godtools.xml.model.Input;
import org.cru.godtools.xml.model.Styles;
import org.cru.godtools.xml.model.Text;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.core.view.ViewCompat;
import butterknife.BindView;
import butterknife.OnFocusChange;
import butterknife.OnTextChanged;

import static org.ccci.gto.android.common.base.Constants.INVALID_STRING_RES;

@UiThread
final class InputViewHolder extends BaseViewHolder<Input> {
    @Nullable
    @BindView(R2.id.label)
    TextView mLabelView;
    @Nullable
    @BindView(R2.id.layout)
    TextInputLayout mInputLayout;
    @BindView(R2.id.input)
    EditText mInputView;

    @Nullable
    private Text mLabel;
    @Nullable
    private Text mPlaceholder;

    InputViewHolder(@NonNull final ViewGroup parent, @Nullable final BaseViewHolder parentViewHolder) {
        super(Input.class, parent, R.layout.tract_content_input, parentViewHolder);
    }

    @Override
    void onBind() {
        super.onBind();
        if (mModel != null) {
            mLabel = mModel.getLabel();
            mPlaceholder = mModel.getPlaceholder();
        } else {
            mLabel = null;
            mPlaceholder = null;
        }

        mRoot.setVisibility(mModel != null && mModel.getType() == Input.Type.HIDDEN ? View.GONE : View.VISIBLE);
        bindLabel();
        bindPlaceholder();
        bindInput();
    }

    // region Lifecycle Events

    @OnTextChanged(value = R2.id.input, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void onTextUpdated() {
        // only update if we are currently in an error state
        if (mInputLayout != null && mInputLayout.isErrorEnabled()) {
            onValidate();
        }
    }

    @OnFocusChange(R2.id.input)
    void onFocusChanged(final boolean hasFocus) {
        if (!hasFocus) {
            onValidate();
        }
    }

    @Override
    boolean onValidate() {
        final String value = getValue();
        final Input.Error error = mModel != null ? mModel.validateValue(value) : null;
        final String msg = error == null ? null : error.msgId != INVALID_STRING_RES ?
                mRoot.getResources().getString(error.msgId, mModel.getName(), value) : error.msg;
        showError(msg);
        return error == null;
    }

    @Override
    void onBuildEvent(@NonNull final Event.Builder builder, final boolean recursive) {
        if (mModel != null) {
            final String value = getValue();
            if (mModel.getName() != null && value != null) {
                builder.field(mModel.getName(), value);
            }
        }
    }

    // endregion Lifecycle Events

    private void bindLabel() {
        final Text labelStyles = mLabel != null ? mLabel : mPlaceholder;
        if (mInputLayout != null) {
            TextInputLayoutUtils.setFocusedTextColor(mInputLayout, Text.getTextColor(labelStyles));
        }
    }

    private void bindPlaceholder() {
        // setup the hint, prefer the label (unless we have a separate label view or no label is defined)
        Text hintText = mLabel;
        if (mLabelView != null || hintText == null) {
            hintText = mPlaceholder;
        }

        // set the hint on the layout or the actual input (based on what's available)
        if (mInputLayout != null) {
            mInputLayout.setHint(Text.getText(hintText));
        } else if (mInputView != null) {
            mInputView.setHint(Text.getText(hintText));
        }

        // update placeholder styles
        final Text hintStyles = mPlaceholder != null ? mPlaceholder : mLabel;
        final int hintColor = Text.getTextColor(hintStyles);
        if (mInputLayout != null) {
            TextInputLayoutUtils.setExpandedTextColor(mInputLayout, hintColor);
        } else if (mInputView != null) {
            mInputView.setHintTextColor(hintColor);
        }
    }

    private void bindInput() {
        // setup inputType
        if (mInputView != null) {
            int inputType = InputType.TYPE_CLASS_TEXT;
            switch (mModel != null ? mModel.getType() : Input.Type.DEFAULT) {
                case EMAIL:
                    inputType |= InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;
                    break;
                case PHONE:
                    inputType = InputType.TYPE_CLASS_PHONE;
                    break;
            }
            mInputView.setInputType(inputType);
        }

        // style the input view
        final Styles stylesParent = Base.getStylesParent(mModel);
        if (mInputView != null) {
            mInputView.setTextColor(Styles.getTextColor(stylesParent));
            ViewCompat.setBackgroundTintList(mInputView,
                                             ColorStateList.valueOf(Styles.getPrimaryColor(stylesParent)));
        }
    }

    private void showError(@Nullable final String error) {
        if (mInputLayout != null) {
            mInputLayout.setError(error);
            mInputLayout.setErrorEnabled(error != null);
        } else if (mInputView != null) {
            mInputView.setError(error);
        }
    }

    @Nullable
    private String getValue() {
        if (mModel != null && mModel.getType() == Input.Type.HIDDEN) {
            return mModel.getValue();
        } else if (mInputView != null) {
            return mInputView.getText().toString();
        }

        return null;
    }
}
