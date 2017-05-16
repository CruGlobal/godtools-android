package org.keynote.godtools.android.api;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;

import org.ccci.gto.android.common.api.okhttp3.util.OkHttpClientUtil;
import org.ccci.gto.android.common.api.retrofit2.converter.LocaleConverterFactory;
import org.ccci.gto.android.common.gson.GsonIgnoreExclusionStrategy;
import org.ccci.gto.android.common.jsonapi.JsonApiConverter;
import org.ccci.gto.android.common.jsonapi.converter.LocaleTypeConverter;
import org.ccci.gto.android.common.jsonapi.retrofit2.JsonApiConverterFactory;
import org.keynote.godtools.android.model.Attachment;
import org.keynote.godtools.android.model.Language;
import org.keynote.godtools.android.model.Tool;
import org.keynote.godtools.android.model.Translation;

import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

import static org.keynote.godtools.android.BuildConfig.BASE_URL;
import static org.keynote.godtools.android.BuildConfig.GROWTH_SPACES_URL;
import static org.keynote.godtools.android.BuildConfig.MOBILE_CONTENT_API;

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
    public final GrowthSpacesApi growthSpaces;
    @NonNull
    public final LegacyApi legacy;

    private GodToolsApi(@NonNull final Context context) {
        mContext = context;

        // create Retrofit APIs
        final Call.Factory okhttp = okhttp();
        final Retrofit retrofit = mobileContentRetrofit()
                .callFactory(okhttp)
                .build();
        languages = retrofit.create(LanguagesApi.class);
        tools = retrofit.create(ToolsApi.class);
        translations = retrofit.create(TranslationsApi.class);
        attachments = retrofit.create(AttachmentsApi.class);

        growthSpaces = new Retrofit.Builder()
                .baseUrl(GROWTH_SPACES_URL)
                .addConverterFactory(gsonConverter())
                .callFactory(okhttp)
                .build()
                .create(GrowthSpacesApi.class);

        legacy = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(SimpleXmlConverterFactory.create())
                .callFactory(okhttp)
                .build()
                .create(LegacyApi.class);
    }

    @Nullable
    @SuppressLint("StaticFieldLeak")
    private static GodToolsApi sInstance;
    @NonNull
    public static synchronized GodToolsApi getInstance(@NonNull final Context context) {
        if (sInstance == null) {
            sInstance = new GodToolsApi(context.getApplicationContext());
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

    private GsonConverterFactory gsonConverter() {
        return GsonConverterFactory.create(
                new GsonBuilder()
                        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                        .setExclusionStrategies(new GsonIgnoreExclusionStrategy())
                        .create());
    }

    @NonNull
    private JsonApiConverter jsonApiConverter() {
        return new JsonApiConverter.Builder()
                .addClasses(Language.class)
                .addClasses(Tool.class)
                .addClasses(Attachment.class)
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
