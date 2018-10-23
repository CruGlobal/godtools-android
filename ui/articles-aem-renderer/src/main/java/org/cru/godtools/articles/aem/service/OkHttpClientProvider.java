package org.cru.godtools.articles.aem.service;

import com.annimon.stream.Stream;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import timber.log.Timber;

public class OkHttpClientProvider {

    public static OkHttpClient.Builder getEnabledTLSSupportedBuilder(OkHttpClient.Builder builder) {

        try {
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(KeyStore.getInstance(KeyStore.getDefaultType()));
            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
            if (trustManagers.length != 1 || !(trustManagers[0]instanceof X509TrustManager)) {
                throw new IllegalStateException("Unexpected default trust managers: " + Arrays.toString(trustManagers));
            }
            X509TrustManager trustManager = (X509TrustManager) trustManagers[0];

            builder.sslSocketFactory(new Tls12SocketFactory(), trustManager);

            ConnectionSpec cs = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS).build();

            builder.connectionSpecs(Collections.singletonList(cs));
        } catch (NoSuchAlgorithmException | IllegalStateException | KeyManagementException e) {
            Timber.e(e);
        } catch (Exception e) {
            Timber.e(e);
        }
        return builder;
    }

    private static class Tls12SocketFactory extends SSLSocketFactory {

        final SSLSocketFactory delegate;

        Tls12SocketFactory() throws NoSuchAlgorithmException, KeyManagementException {
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, null, null);
            this.delegate = context.getSocketFactory();
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
            return enableTLSOnSocket(delegate.createSocket(s, host, port, autoClose));
        }

        @Override
        public Socket createSocket(String host, int port) throws IOException {
            return enableTLSOnSocket(delegate.createSocket(host, port));
        }

        @Override
        public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException {
            return enableTLSOnSocket(delegate.createSocket(host, port, localHost, localPort));
        }

        @Override
        public Socket createSocket(InetAddress host, int port) throws IOException {
            return enableTLSOnSocket(delegate.createSocket(host, port));
        }

        @Override
        public Socket createSocket(InetAddress address, int port, InetAddress localAddress,
                                   int localPort) throws IOException {
            return enableTLSOnSocket(delegate.createSocket(address, port, localAddress, localPort));
        }

        private Socket enableTLSOnSocket(Socket socket) {
            if (socket instanceof SSLSocket) {
                SSLSocket sslSocket = (SSLSocket) socket;
                String[] supportedProtocols = sslSocket.getSupportedProtocols();
                ArrayList<String> enabledProtocols = new ArrayList<>(Arrays.asList(sslSocket.getEnabledProtocols()));
                Stream.of(supportedProtocols).forEach(ep -> {
                    if (!enabledProtocols.contains(ep)) {
                        enabledProtocols.add(ep);
                    }
                });
                sslSocket.setEnabledProtocols(enabledProtocols.toArray(new String[0]));
            }
            return socket;
        }
    }
}
