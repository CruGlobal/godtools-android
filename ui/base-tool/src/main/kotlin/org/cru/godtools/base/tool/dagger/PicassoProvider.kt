package org.cru.godtools.base.tool.dagger

import android.content.Context
import com.squareup.picasso.Picasso
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
internal interface PicassoProvider {
    val picasso: Picasso
}

internal val Context.picasso get() = EntryPointAccessors.fromApplication<PicassoProvider>(this).picasso
