/*
 * Copyright (c) 2015, The Dattack team (http://www.dattack.com)
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
package com.dattack.aranea.engine;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;

/**
 * @author cvarela
 * @since 0.1
 */
public class ResourceCoordinates {

    private static final int DEFAULT_HTTP_PORT = 80;
    private static final int DEFAULT_HTTPS_PORT = 443;

    private final URI uri;
    private final URI referer;
    private final Map<String, Object> navigationContext;

    // the same URI representation than 'uri' parameter but ignoring the schema and fragment parts
    private URI internalUri;

    public ResourceCoordinates(final URI uri) {
        this(uri, null);
    }

    public ResourceCoordinates(final URI uri, final URI referer) {
        this.uri = uri;
        this.referer = referer == null ? null : referer.normalize();
        this.internalUri = getInternalUri();
        this.navigationContext = new HashMap<>();
    }

    public ResourceCoordinates addNavigationContext(final Map<String, Object> map) {

        if (map != null) {
            navigationContext.putAll(map);
        }
        return this;
    }

    public ResourceCoordinates addNavigationContext(final String key, final Object value) {

        if (key != null) {
            navigationContext.put(key, value);
        }
        return this;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ResourceCoordinates other = (ResourceCoordinates) obj;
        // ignore protocol
        return internalUri.equals(other.internalUri);
    }

    private URI getInternalUri() {

        if (internalUri == null) {
            try {
                internalUri = new URI(null, // schema
                        uri.getUserInfo(), // user info
                        uri.getHost(), // host
                        normalizePort(), // port
                        uri.normalize().getPath(), // path
                        uri.getQuery(), // query
                        null); // fragment
            } catch (final URISyntaxException e) {
                // this should never happen
                internalUri = uri;
            }
        }
        return internalUri;
    }

    public Map<String, Object> getNavigationContext() {
        return navigationContext;
    }

    public URI getReferer() {
        return referer;
    }

    public URI getUri() {
        return uri;
    }

    @Override
    public int hashCode() {
        return internalUri.hashCode();
    }

    private int normalizePort() {

        int port;
        switch (uri.getScheme().toLowerCase()) {
        case "http":
            port = uri.getPort() == DEFAULT_HTTP_PORT ? -1 : uri.getPort();
            break;
        case "https":
            port = uri.getPort() == DEFAULT_HTTPS_PORT ? -1 : uri.getPort();
            break;
        default:
            port = uri.getPort();
            break;
        }

        return port;
    }

    @Override
    public String toString() {
        return String.format("ResourceCoordinates [uri=%s, referer=%s]", uri, ObjectUtils.toString(referer, ""));
    }
}
