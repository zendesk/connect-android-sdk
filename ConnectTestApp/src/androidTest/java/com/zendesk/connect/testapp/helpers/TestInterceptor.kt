package com.zendesk.connect.testapp.helpers

import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Response

class TestInterceptor(testUrlString: String): Interceptor {

    private val testUrl = HttpUrl.parse(testUrlString)

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()

        testUrl?.let {
            val newUrl = request.url().newBuilder()
                    .scheme(testUrl.scheme())
                    .host(testUrl.host())
                    .port(testUrl.port())
                    .build()

            request = request.newBuilder()
                    .url(newUrl)
                    .build()
        }

        return chain.proceed(request)
    }

}