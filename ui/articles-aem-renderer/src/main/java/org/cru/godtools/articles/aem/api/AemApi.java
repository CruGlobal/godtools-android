package org.cru.godtools.articles.aem.api;

import android.net.Uri;

import org.ccci.gto.android.common.api.retrofit2.converter.JSONObjectConverterFactory;
import org.ccci.gto.android.common.okhttp3.util.OkHttpClientUtil;
import org.ccci.gto.android.common.util.DynamicSSLSocketFactory;
import org.json.JSONObject;

import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLSocketFactory;

import androidx.annotation.NonNull;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.TlsVersion;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Streaming;
import retrofit2.http.Url;
import timber.log.Timber;

public interface AemApi {
    @GET
    Call<JSONObject> getJson(@Url String uri, @Query("_") long timestamp);

    @GET
    Call<String> downloadArticle(@Url String uri);

    @GET
    @Streaming
    Call<ResponseBody> downloadResource(@Url Uri uri);

    static AemApi buildInstance(@NonNull final String uri) {
        // create OkHttp client
        final OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS);
        try {
            // customize the SSLSocketFactory for this OkHttpClient
            final SSLSocketFactory factory = DynamicSSLSocketFactory.create()
                    // enable TLS 1.1 and TLS 1.2 for older versions of android that support it but don't enable it
                    .addEnabledProtocols(TlsVersion.TLS_1_1.javaName(), TlsVersion.TLS_1_2.javaName())
                    .build();
            builder.sslSocketFactory(factory);
        } catch ( IllegalStateException | NoSuchAlgorithmException e) {
            Timber.tag("AemApi")
                    .d(e, "Error creating the DynamicSSLSocketFactory");
        }
        final OkHttpClient okHttp = OkHttpClientUtil.attachGlobalInterceptors(builder).build();

        // create RetroFit API
        return new Retrofit.Builder().baseUrl(uri)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(new JSONObjectConverterFactory())
                .callFactory(okHttp)
                .build().create(AemApi.class);
    }
}
