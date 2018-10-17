package com.zendesk.connect;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;


public class ClientInterceptors {
    public static class OutboundClientInterceptor implements Interceptor {

        private static final String HEADER_NAME = "X-Outbound-Client";
        private String headerValue;

        public OutboundClientInterceptor(String headerValue) {
            this.headerValue = headerValue;
        }

        protected String getHeaderValue() {
            return headerValue;
        }

        @Override
        public Response intercept(Interceptor.Chain chain) throws IOException {
            Request.Builder requestHeadersBuilder = chain.request().newBuilder();

            requestHeadersBuilder.addHeader(HEADER_NAME, getHeaderValue());

            return chain.proceed(requestHeadersBuilder.build());
        }

    }
    public static class OutboundGUIDInterceptor implements Interceptor {

        private static final String HEADER_NAME = "X-Outbound-GUID";
        private String headerValue;

        public OutboundGUIDInterceptor(String headerValue) {
            this.headerValue = headerValue;
        }

        protected String getHeaderValue() {
            return headerValue;
        }

        @Override
        public Response intercept(Interceptor.Chain chain) throws IOException {
            Request.Builder requestHeadersBuilder = chain.request().newBuilder();

            requestHeadersBuilder.addHeader(HEADER_NAME, getHeaderValue());

            return chain.proceed(requestHeadersBuilder.build());
        }

    }
    public static class OutboundKeyInterceptor implements Interceptor {

        private static final String HEADER_NAME = "X-Outbound-Key";
        private String headerValue;

        public OutboundKeyInterceptor(String headerValue) {
            this.headerValue = headerValue;
        }

        protected String getHeaderValue() {
            return headerValue;
        }

        @Override
        public Response intercept(Interceptor.Chain chain) throws IOException {
            Request.Builder requestHeadersBuilder = chain.request().newBuilder();

            requestHeadersBuilder.addHeader(HEADER_NAME, getHeaderValue());

            return chain.proceed(requestHeadersBuilder.build());
        }

    }
}
