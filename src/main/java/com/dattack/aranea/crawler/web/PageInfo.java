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
package com.dattack.aranea.crawler.web;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.jsoup.Connection.Response;

/**
 * @author cvarela
 * @since 0.1
 */
public class PageInfo {

    private final Page page;
    private final int statusCode;
    private final String statusMessage;
    private final String charset;
    private final String contentType;
    private final Set<URI> newUris;
    private final Set<URI> visitedUris;
    private final Set<URI> ignoredUris;
    private final Set<String> ignoredLinks;

    public PageInfo(final Page page, final Response response) {
        this.page = page;
        this.statusCode = response.statusCode();
        this.statusMessage = response.statusMessage();
        this.charset = response.charset();
        this.contentType = response.contentType();
        this.newUris = new HashSet<>();
        this.visitedUris = new HashSet<>();
        this.ignoredUris = new HashSet<>();
        this.ignoredLinks = new HashSet<>();
    }

    public PageInfo(final Page page, final String message) {
        this(page, -1, message);
    }

    public PageInfo(final Page page, final int statusCode, final String message) {
        this.page = page;
        this.statusCode = statusCode;
        this.statusMessage = message;
        this.charset = "";
        this.contentType = "";
        this.newUris = new HashSet<>();
        this.visitedUris = new HashSet<>();
        this.ignoredUris = new HashSet<>();
        this.ignoredLinks = new HashSet<>();
    }

    public void addIgnoredLink(final String link) {
        if (link != null) {
            this.ignoredLinks.add(link);
        }
    }

    public void addIgnoredUri(final URI uri) {
        if (uri != null) {
            this.ignoredUris.add(uri);
        }
    }

    public void addNewUri(final URI uri) {
        if (uri != null) {
            this.newUris.add(uri);
        }
    }

    public void addVisitedUri(final URI uri) {
        if (uri != null) {
            this.visitedUris.add(uri);
        }
    }

    public String getCharset() {
        return charset;
    }

    public String getContentType() {
        return contentType;
    }

    public Set<String> getIgnoredLinks() {
        return ignoredLinks;
    }

    public Set<URI> getIgnoredUris() {
        return ignoredUris;
    }

    public Set<URI> getNewUris() {
        return newUris;
    }

    public Page getPage() {
        return page;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public Set<URI> getVisitedUris() {
        return visitedUris;
    }

    public boolean isDuplicatedLink(final URI uri) {
        if (uri == null) {
            return true;
        }
        return newUris.contains(uri) || visitedUris.contains(uri);
    }
}
