package org.cru.godtools.articles.aem.api;

import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;

import com.google.common.collect.ImmutableList;

import org.ccci.gto.android.common.api.retrofit2.converter.JSONObjectConverterFactory;
import org.ccci.gto.android.common.okhttp3.util.OkHttpClientUtil;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import okhttp3.ConnectionSpec;
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


            if (Build.VERSION.SDK_INT < 22) {
                try {
                    SSLContext sc = SSLContext.getInstance("TLS");
                    sc.init(null, null, null);
                    builder.sslSocketFactory(new Tls12SocketFactory(sc.getSocketFactory()));
                    ConnectionSpec connectionSpec = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                            .tlsVersions(TlsVersion.TLS_1_2)
                            .build();
                    builder.connectionSpecs(ImmutableList.of(connectionSpec));
                } catch (NoSuchAlgorithmException | KeyManagementException e) {
                    Timber.e(e);
                }
            }
        final OkHttpClient okHttp = OkHttpClientUtil.attachGlobalInterceptors(builder).build();
            // create RetroFit API
            return new Retrofit.Builder().baseUrl(uri)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(new JSONObjectConverterFactory())
                    .callFactory(okHttp)
                    .build().create(AemApi.class);

    }

    class Tls12SocketFactory extends SSLSocketFactory {
        private static final String[] TLS_V12_ONLY = {"TLS"};

        final SSLSocketFactory delegate;

        public Tls12SocketFactory(SSLSocketFactory base) {
            this.delegate = base;
        }

        @Override
        public String[] getDefaultCipherSuites() {
            return delegate.getDefaultCipherSuites();
        }

        @Override
        public String[] getSupportedCipherSuites() {
            return delegate.getSupportedCipherSuites();
        }

        @Override
        public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
            return patch(delegate.createSocket(s, host, port, autoClose));
        }

        @Override
        public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
            return patch(delegate.createSocket(host, port));
        }

        @Override
        public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException, UnknownHostException {
            return patch(delegate.createSocket(host, port, localHost, localPort));
        }

        @Override
        public Socket createSocket(InetAddress host, int port) throws IOException {
            return patch(delegate.createSocket(host, port));
        }

        @Override
        public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
            return patch(delegate.createSocket(address, port, localAddress, localPort));
        }

        private Socket patch(Socket s) {
            if (s instanceof SSLSocket) {
                ((SSLSocket) s).setEnabledProtocols(TLS_V12_ONLY);
            }
            return s;
        }
    }
}
