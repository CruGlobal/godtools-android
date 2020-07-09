package org.cru.godtools.tract.viewmodel;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.google.android.material.textfield.TextInputLayout;

import org.cru.godtools.base.model.Event;
import org.cru.godtools.tract.R2;
import org.cru.godtools.tract.databinding.TractContentInputBinding;
import org.cru.godtools.xml.model.Input;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import butterknife.BindView;
import butterknife.OnFocusChange;
import butterknife.OnTextChanged;

@UiThread
public final class InputViewHolder extends BaseViewHolder<Input> {
    private final TractContentInputBinding mBinding;

    @Nullable
    @BindView(R2.id.layout)
    TextInputLayout mInputLayout;
    @BindView(R2.id.input)
    EditText mInputView;

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
        if (mBinding.getModel() != getModel()) {
            mBinding.setError(null);
        }
        mBinding.setModel(getModel());
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
        final Input.Error error = mModel != null ? mModel.validateValue(getValue()) : null;
        mBinding.setError(error);
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
