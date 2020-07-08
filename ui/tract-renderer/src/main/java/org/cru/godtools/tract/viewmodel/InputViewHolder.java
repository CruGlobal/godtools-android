package org.cru.godtools.tract.viewmodel;

import android.content.Context;
import android.content.res.ColorStateList;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.google.android.material.textfield.TextInputLayout;

import org.ccci.gto.android.common.material.textfield.TextInputLayoutKt;
import org.cru.godtools.base.model.Event;
import org.cru.godtools.tract.R2;
import org.cru.godtools.tract.databinding.TractContentInputBinding;
import org.cru.godtools.xml.model.Input;
import org.cru.godtools.xml.model.InputKt;
import org.cru.godtools.xml.model.Text;
import org.cru.godtools.xml.model.TextKt;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import butterknife.BindView;
import butterknife.OnFocusChange;
import butterknife.OnTextChanged;

import static org.ccci.gto.android.common.base.Constants.INVALID_STRING_RES;

@UiThread
public final class InputViewHolder extends BaseViewHolder<Input> {
    private final TractContentInputBinding mBinding;

    @Nullable
    @BindView(R2.id.layout)
    TextInputLayout mInputLayout;
    @BindView(R2.id.input)
    EditText mInputView;

    @Nullable
    private Text mLabel;
    @Nullable
    private Text mPlaceholder;

    private InputViewHolder(@NonNull final TractContentInputBinding binding,
                           @Nullable final BaseViewHolder parentViewHolder) {
        super(Input.class, binding.getRoot(), parentViewHolder);
        mBinding = binding;
    }

    public static InputViewHolder create(@NonNull final ViewGroup parent,
                                         @Nullable final BaseViewHolder parentViewHolder) {
        return new InputViewHolder(
                TractContentInputBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false),
                parentViewHolder);
    }

    @Override
    protected void onBind() {
        super.onBind();
        mBinding.setModel(getModel());
        if (mModel != null) {
            mLabel = mModel.getLabel();
            mPlaceholder = mModel.getPlaceholder();
        } else {
            mLabel = null;
            mPlaceholder = null;
        }

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
        InputMethodManager keyboard = (InputMethodManager) mInputView.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (!hasFocus) {
            onValidate();
            keyboard.hideSoftInputFromWindow(mInputView.getWindowToken(), 0);
        } else {
            keyboard.showSoftInput(mInputView, 0);
        }
    }

    @Override
    public boolean onValidate() {
        final String value = getValue();
        final Input.Error error = mModel != null ? mModel.validateValue(value) : null;
        final String msg = error == null ? null : error.msgId != INVALID_STRING_RES ?
                mRoot.getResources().getString(error.msgId, mModel.getName(), value) : error.msg;
        showError(msg);
        return error == null;
    }

    @Override
    public void onBuildEvent(@NonNull final Event.Builder builder, final boolean recursive) {
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
            TextInputLayoutKt.setFocusedTextColor(mInputLayout, TextKt.getTextColor(labelStyles));
        }
    }

    private void bindPlaceholder() {
        // setup the hint, prefer the label (unless we have a separate label view or no label is defined)
        Text hintText = mLabel;
        if (hintText == null) {
            hintText = mPlaceholder;
        }

        // set the hint on the layout or the actual input (based on what's available)
        if (mInputLayout != null) {
            mInputLayout.setHint(TextKt.getText(hintText));
        } else if (mInputView != null) {
            mInputView.setHint(TextKt.getText(hintText));
        }

        // update placeholder styles
        final Text hintStyles = mPlaceholder != null ? mPlaceholder : mLabel;
        final int hintColor = TextKt.getTextColor(hintStyles);
        if (mInputLayout != null) {
            mInputLayout.setDefaultHintTextColor(ColorStateList.valueOf(hintColor));
        } else if (mInputView != null) {
            mInputView.setHintTextColor(hintColor);
        }
    }

    private void bindInput() {
        // setup inputType
        if (mInputView != null) {
            int inputType = InputType.TYPE_CLASS_TEXT;
            switch (InputKt.getType(mModel)) {
                case EMAIL:
                    inputType |= InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;
                    break;
                case PHONE:
                    inputType = InputType.TYPE_CLASS_PHONE;
                    break;
            }
            mInputView.setInputType(inputType);
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
