package org.cru.godtools.base.tool.activity;

import org.cru.godtools.xml.model.Manifest;

import androidx.annotation.NonNull;

public abstract class BaseArticleActivity extends BaseSingleToolActivity {
    public BaseArticleActivity(final boolean immersive) {
        super(immersive);
    }

    public BaseArticleActivity(final boolean immersive, final boolean requireTool) {
        super(immersive, requireTool);
    }

    @Override
    protected boolean isSupportedType(@NonNull final Manifest.Type type) {
        return type == Manifest.Type.ARTICLE;
    }
}
