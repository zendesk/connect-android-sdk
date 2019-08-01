package com.zendesk.connect;

import android.os.Build;

import com.zendesk.logger.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.util.Collections;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.TlsVersion;

/**
 * {@link SSLSocketFactory} patch for force TLS 1.2 on Android pre-Lollipop. This is reused code
 * from the other SDKs but uses a non deprecated version of {@link OkHttpClient.Builder#sslSocketFactory}
 */
class Tls1Dot2SocketFactory extends SSLSocketFactory {

    private static final String LOG_TAG = "Tls1Dot2SocketFactory";

    private static final String[] TLS_V1_2_ONLY = { TlsVersion.TLS_1_2.javaName() };

    private final SSLSocketFactory delegate;

    static OkHttpClient.Builder enableTls1Dot2OnPreLollipop(OkHttpClient.Builder client) {

        //only patch android versions that need to be patched
        if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < 21) {
            try {
                SSLContext sc = SSLContext.getInstance("TLSv1.2");
                sc.init(null, null, null);

                // The version of the sslSocketFactory method without TrustManager is now deprecated
                TrustManagerFactory trustManagerFactory = TrustManagerFactory
                        .getInstance(TrustManagerFactory.getDefaultAlgorithm());
                trustManagerFactory.init((KeyStore) null);
                TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();

                client.sslSocketFactory(new Tls1Dot2SocketFactory(sc.getSocketFactory()),
                        (X509TrustManager)trustManagers[0]);

                //limit to tls version 1.2
                ConnectionSpec cs = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                        .tlsVersions(TlsVersion.TLS_1_2)
                        .build();

                client.connectionSpecs(Collections.singletonList(cs));
                Logger.d(LOG_TAG, "Applied TLS 1.2 patch");
            } catch (Exception e) {
                Logger.e(LOG_TAG, "Error while setting TLS 1.2", e);
            }
        } else {
            Logger.d(LOG_TAG, "Skipping TLS 1.2 patch");
            //only use modern tls 1.2 & 1.3 on newer devices
            client.connectionSpecs(Collections.singletonList(new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                    .tlsVersions(TlsVersion.TLS_1_2, TlsVersion.TLS_1_3)
                    .build()));
        }
        return client;
    }

    Tls1Dot2SocketFactory(SSLSocketFactory base) {
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
    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException {
        return patch(delegate.createSocket(socket, host, port, autoClose));
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
        return patch(delegate.createSocket(host, port));
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort)
            throws IOException, UnknownHostException {
        return patch(delegate.createSocket(host, port, localHost, localPort));
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        return patch(delegate.createSocket(host, port));
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort)
            throws IOException {
        return patch(delegate.createSocket(address, port, localAddress, localPort));
    }

    private static Socket patch(Socket s) {
        if (s instanceof SSLSocket) {
            ((SSLSocket) s).setEnabledProtocols(TLS_V1_2_ONLY);
        }
        return s;
    }
}
