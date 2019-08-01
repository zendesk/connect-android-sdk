package com.zendesk.connect;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;


class ClientInterceptors {

    static class OutboundKeyInterceptor implements Interceptor {

        private static final String HEADER_NAME = "X-Outbound-Key";
        private String headerValue;

        OutboundKeyInterceptor(String headerValue) {
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

    static class OutboundClientInterceptor implements Interceptor {

        private static final String HEADER_NAME = "X-Outbound-Client";
        private String headerValue;

        OutboundClientInterceptor(String headerValue) {
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

    static class OutboundGuidInterceptor implements Interceptor {

        private static final String HEADER_NAME = "X-Outbound-GUID";
        private String headerValue;

        OutboundGuidInterceptor(String headerValue) {
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
