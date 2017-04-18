package org.keynote.godtools.android.api;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.ccci.gto.android.common.api.okhttp3.util.OkHttpClientUtil;
import org.ccci.gto.android.common.api.retrofit2.converter.LocaleConverterFactory;
import org.ccci.gto.android.common.jsonapi.JsonApiConverter;
import org.ccci.gto.android.common.jsonapi.converter.LocaleTypeConverter;
import org.ccci.gto.android.common.jsonapi.retrofit2.JsonApiConverterFactory;
import org.keynote.godtools.android.model.Resource;
import org.keynote.godtools.android.model.Translation;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

import static org.keynote.godtools.android.BuildConfig.MOBILE_CONTENT_API;

public class GodToolsV2Api {
    @NonNull
    private final Context mContext;

    @NonNull
    public final ResourcesApi resources;

    private GodToolsV2Api(@NonNull final Context context) {
        mContext = context;

        // create Retrofit APIs
        final Retrofit.Builder retrofit = mobileContentRetrofit();
        resources = retrofit
                .callFactory(okhttp())
                .build()
                .create(ResourcesApi.class);
    }

    @Nullable
    @SuppressLint("StaticFieldLeak")
    private static GodToolsV2Api sInstance;
    @NonNull
    public static synchronized GodToolsV2Api getInstance(@NonNull final Context context) {
        if (sInstance == null) {
            sInstance = new GodToolsV2Api(context.getApplicationContext());
        }
        return sInstance;
    }

    @NonNull
    private Retrofit.Builder mobileContentRetrofit() {
        return new Retrofit.Builder()
                .baseUrl(MOBILE_CONTENT_API)
                // attach the various converter factories
                .addConverterFactory(new LocaleConverterFactory())
                .addConverterFactory(JsonApiConverterFactory.create(jsonApiConverter()));
    }

    @NonNull
    private JsonApiConverter jsonApiConverter() {
        return new JsonApiConverter.Builder()
                .addClasses(Resource.class)
                .addClasses(Translation.class)
                .addConverters(new LocaleTypeConverter())
                .build();
    }

    @NonNull
    private OkHttpClient okhttp() {
        final OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS);
        return OkHttpClientUtil.attachGlobalInterceptors(builder).build();
    }
}
