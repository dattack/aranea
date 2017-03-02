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

import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

import com.dattack.aranea.engine.ResourceCoordinates;
import com.dattack.jtoolbox.patterns.Builder;

/**
 * @author cvarela
 * @since 0.1
 */
public class HttpResourceRequest {

    public static final class HttpResourceRequestBuilder implements Builder<HttpResourceRequest> {

        private final ResourceCoordinates resourceCoordinates;
        private final List<Header> headerList;

        public HttpResourceRequestBuilder(final ResourceCoordinates resourceCoordinates) {
            this.resourceCoordinates = resourceCoordinates;
            this.headerList = new ArrayList<Header>();
        }

        @Override
        public HttpResourceRequest build() {
            return new HttpResourceRequest(this);
        }

        public HttpResourceRequestBuilder withHeader(final String name, final String value) {
            this.headerList.add(new BasicHeader(name, value));
            return this;
        }
    }

    private final ResourceCoordinates resourceCoordinates;
    private final List<Header> headerList;

    private HttpResourceRequest(final HttpResourceRequestBuilder builder) {
        this.resourceCoordinates = builder.resourceCoordinates;
        this.headerList = builder.headerList;
    }

    public ResourceCoordinates getResourceCoordinates() {
        return resourceCoordinates;
    }

    public List<Header> getHeaderList() {
        return headerList;
    }
}
