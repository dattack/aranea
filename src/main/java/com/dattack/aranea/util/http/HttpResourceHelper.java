/*
 * Copyright (c) 2017, The Dattack team (http://www.dattack.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dattack.aranea.util.http;

import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.util.EntityUtils;

/**
 * @author cvarela
 * @since 0.1
 */
public class HttpResourceHelper {

    private static HttpGet createGetMethod(final HttpResourceRequest request) {

        HttpGet httpGet = new HttpGet(request.getResourceCoordinates().getUri());
        for (final Header header : request.getHeaderList()) {
            httpGet.addHeader(header);
        }
        return httpGet;
    }

    private static CloseableHttpClient createHttpClient() {

        final int connectTimeout = 30 * 1000; // TODO: configure timeout
        RequestConfig requestConfig = RequestConfig.custom() //
                .setConnectTimeout(connectTimeout) //
                .setCookieSpec(CookieSpecs.STANDARD) //
                .build();

        HttpClientBuilder builder = HttpClients.custom() //
                .setDefaultRequestConfig(requestConfig) //
                .setRedirectStrategy(new LaxRedirectStrategy()) //
                .setUserAgent("Aranea 0.1");

        // TODO: complete the custom configuration of this HttpClient object
        return builder.build();
    }

    public static HttpResourceResponse get(final HttpResourceRequest request)
            throws IOException, ClientProtocolException {

        try (CloseableHttpClient httpclient = createHttpClient();
                CloseableHttpResponse response = httpclient.execute(createGetMethod(request))) {

            HttpEntity entity = response.getEntity();
            ContentType contentType = ContentType.getOrDefault(entity);
            Charset charset = contentType.getCharset();

            String content = IOUtils.toString(entity.getContent(), charset);
            EntityUtils.consume(entity);

            return new HttpResourceResponse(request, response.getStatusLine(), contentType, content);
        }
    }
}
