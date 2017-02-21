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
package com.dattack.aranea.engine.web.crawler;

import java.net.URI;

/**
 * @author cvarela
 * @since 0.1
 */
public class Page {

    private final URI uri;
    private final URI referer;

    public Page(final URI uri) {
        this(uri, null);
    }

    public Page(final URI uri, final URI referer) {
        this.uri = uri;
        this.referer = referer;
    }

    public URI getUri() {
        return uri;
    }

    public URI getReferer() {
        return referer;
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
        Page other = (Page) obj;
        return uri.equals(other.getUri());
    }

    @Override
    public int hashCode() {
        return uri.hashCode();
    }
}
