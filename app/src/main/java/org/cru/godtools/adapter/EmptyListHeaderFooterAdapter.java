package org.cru.godtools.adapter;

import androidx.annotation.NonNull;

public class EmptyListHeaderFooterAdapter extends BaseHeaderFooterAdapter {
    public static class Builder extends BaseHeaderFooterAdapter.Builder<Builder> {
        public EmptyListHeaderFooterAdapter build() {
            return new EmptyListHeaderFooterAdapter(this);
        }
    }

    EmptyListHeaderFooterAdapter(@NonNull final Builder builder) {
        super(builder);
    }
}
