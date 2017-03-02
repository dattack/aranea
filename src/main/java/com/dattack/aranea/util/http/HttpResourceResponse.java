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

import java.net.URI;

import org.apache.commons.lang.StringUtils;
import org.apache.http.StatusLine;
import org.apache.http.entity.ContentType;

/**
 * @author cvarela
 * @since 0.1
 */
public class HttpResourceResponse {

    private final HttpResourceRequest request;
    private final StatusLine statusLine;
    private final ContentType contentType;
    private final String data;

    public HttpResourceResponse(final HttpResourceRequest request, final StatusLine statusLine,
            final ContentType contentType, final String data) {
        this.request = request;
        this.statusLine = statusLine;
        this.contentType = contentType;
        this.data = data;
    }

    public URI getResourceUri() {
        return request.getResourceCoordinates().getUri();
    }

    public HttpResourceRequest getRequest() {
        return request;
    }

    public StatusLine getStatusLine() {
        return statusLine;
    }

    public ContentType getContentType() {
        return contentType;
    }

    public String getData() {
        return data;
    }
    
    public boolean hasData() {
        return StringUtils.isNotBlank(getData());
    }
}
