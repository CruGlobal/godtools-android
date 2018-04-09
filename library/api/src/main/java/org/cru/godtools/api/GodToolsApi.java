package org.cru.godtools.api;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.ccci.gto.android.common.api.okhttp3.util.OkHttpClientUtil;
import org.ccci.gto.android.common.api.retrofit2.converter.LocaleConverterFactory;
import org.ccci.gto.android.common.jsonapi.JsonApiConverter;
import org.ccci.gto.android.common.jsonapi.converter.LocaleTypeConverter;
import org.ccci.gto.android.common.jsonapi.retrofit2.JsonApiConverterFactory;
import org.cru.godtools.api.model.ToolViews;
import org.cru.godtools.model.Attachment;
import org.cru.godtools.model.Followup;
import org.cru.godtools.model.Language;
import org.cru.godtools.model.Translation;
import org.cru.godtools.model.jsonapi.ToolTypeConverter;
import org.keynote.godtools.android.model.Tool;

import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

public class GodToolsApi {
    @NonNull
    private final Context mContext;

    @NonNull
    public final LanguagesApi languages;
    @NonNull
    public final ToolsApi tools;
    @NonNull
    public final TranslationsApi translations;
    @NonNull
    public final AttachmentsApi attachments;
    @NonNull
    public final FollowupApi followups;
    @NonNull
    public final ViewsApi views;

    private GodToolsApi(@NonNull final Context context, @NonNull final String apiUri) {
        mContext = context;

        // create Retrofit APIs
        final Call.Factory okhttp = okhttp();
        final Retrofit retrofit = mobileContentRetrofit(apiUri)
                .callFactory(okhttp)
                .build();
        languages = retrofit.create(LanguagesApi.class);
        tools = retrofit.create(ToolsApi.class);
        translations = retrofit.create(TranslationsApi.class);
        attachments = retrofit.create(AttachmentsApi.class);
        followups = retrofit.create(FollowupApi.class);
        views = retrofit.create(ViewsApi.class);
    }

    @Nullable
    @SuppressLint("StaticFieldLeak")
    private static GodToolsApi sInstance;
    public static synchronized void configure(@NonNull final Context context, @NonNull final String apiUri) {
        if (sInstance != null) {
            throw new IllegalStateException("Attempted to configure GodToolsApi multiple times");
        }

        sInstance = new GodToolsApi(context.getApplicationContext(), apiUri);
    }


    @NonNull
    public static synchronized GodToolsApi getInstance(@NonNull final Context context) {
        if (sInstance == null) {
            throw new IllegalStateException("Attempted to use GodTools API before it was configured");
        }
        return sInstance;
    }

    @NonNull
    private Retrofit.Builder mobileContentRetrofit(@NonNull final String apiUri) {
        return new Retrofit.Builder()
                .baseUrl(apiUri)
                // attach the various converter factories
                .addConverterFactory(new LocaleConverterFactory())
                .addConverterFactory(JsonApiConverterFactory.create(jsonApiConverter()));
    }

    @NonNull
    private JsonApiConverter jsonApiConverter() {
        return new JsonApiConverter.Builder()
                .addClasses(Language.class)
                .addClasses(Tool.class, ToolViews.class)
                .addClasses(Attachment.class)
                .addClasses(Translation.class)
                .addClasses(Followup.class)
                .addConverters(new ToolTypeConverter())
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
