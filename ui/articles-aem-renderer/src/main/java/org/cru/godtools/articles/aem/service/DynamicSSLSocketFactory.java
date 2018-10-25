package org.cru.godtools.articles.aem.service;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DynamicSSLSocketFactory extends SSLSocketFactory {
    private final SSLSocketFactory mDelegate;
    private final Set<String> mEnabledProtocolsToAdd = new HashSet<>();

    // region Initialization

    DynamicSSLSocketFactory(@NonNull final Builder builder) {
        mDelegate = builder.mFactory;
        mEnabledProtocolsToAdd.addAll(builder.mEnabledProtocolsToAdd);
    }

    public static Builder create() throws NoSuchAlgorithmException {
        return new Builder(SSLContext.getDefault().getSocketFactory());
    }

    public static Builder wrap(@NonNull final SSLSocketFactory factory) {
        return new Builder(factory);
    }

    // endregion Initialization

    // region SSLSocketFactory Overrides

    @Override
    public String[] getDefaultCipherSuites() {
        return mDelegate.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return mDelegate.getSupportedCipherSuites();
    }

    @Override
    public Socket createSocket(final Socket s, final String host, final int port, final boolean autoClose)
            throws IOException {
        return adjustSocket(mDelegate.createSocket(s, host, port, autoClose));
    }

    @Override
    public Socket createSocket(final String host, final int port) throws IOException {
        return adjustSocket(mDelegate.createSocket(host, port));
    }

    @Override
    public Socket createSocket(final String host, final int port, final InetAddress localHost, final int localPort)
            throws IOException {
        return adjustSocket(mDelegate.createSocket(host, port, localHost, localPort));
    }

    @Override
    public Socket createSocket(final InetAddress host, final int port) throws IOException {
        return adjustSocket(mDelegate.createSocket(host, port));
    }

    @Override
    public Socket createSocket(final InetAddress address, final int port, final InetAddress localAddress,
                               final int localPort) throws IOException {
        return adjustSocket(mDelegate.createSocket(address, port, localAddress, localPort));
    }

    // endregion SSLSocketFactory Overrides

    private Socket adjustSocket(@Nullable final Socket socket) {
        if (socket instanceof SSLSocket) {
            final SSLSocket sslSocket = (SSLSocket) socket;

            // update the enabled protocols if required
            if (!mEnabledProtocolsToAdd.isEmpty()) {
                final Set<String> enabledProtocols = new HashSet<>();
                Collections.addAll(enabledProtocols, sslSocket.getEnabledProtocols());
                for (final String protocol : sslSocket.getSupportedProtocols()) {
                    if (mEnabledProtocolsToAdd.contains(protocol)) {
                        enabledProtocols.add(protocol);
                    }
                }
                sslSocket.setEnabledProtocols(enabledProtocols.toArray(new String[0]));
            }
        }
        return socket;
    }

    public static final class Builder {
        @NonNull
        final SSLSocketFactory mFactory;
        final Set<String> mEnabledProtocolsToAdd = new HashSet<>();

        Builder(@NonNull final SSLSocketFactory factory) {
            mFactory = factory;
        }

        public Builder addEnabledProtocols(@NonNull final String... protocols) {
            Collections.addAll(mEnabledProtocolsToAdd, protocols);
            return this;
        }

        public DynamicSSLSocketFactory build() {
            return new DynamicSSLSocketFactory(this);
        }
    }
}
