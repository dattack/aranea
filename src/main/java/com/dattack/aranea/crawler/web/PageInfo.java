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

/**
 * @author carlos
 *
 */
public class PageInfo {

    private final Page page;
    private final Set<URI> newUris;
    private final Set<URI> visitedUris;
    private final Set<URI> ignoredUris;
    private final Set<String> ignoredLinks;

    public PageInfo(final Page page) {
        this.page = page;
        this.newUris = new HashSet<>();
        this.visitedUris = new HashSet<>();
        this.ignoredUris = new HashSet<>();
        this.ignoredLinks = new HashSet<>();
    }

    public boolean isDuplicatedLink(final URI uri) {
        return newUris.contains(uri) || visitedUris.contains(uri);
    }

    public void addNewUri(final URI uri) {
        this.newUris.add(uri);
    }

    public void addVisitedUri(final URI uri) {
        this.visitedUris.add(uri);
    }

    public void addIgnoredUri(final URI uri) {
        this.ignoredUris.add(uri);
    }

    public void addIgnoredLink(final String link) {
        this.ignoredLinks.add(link);
    }

    public Page getPage() {
        return page;
    }

    public Set<URI> getNewUris() {
        return newUris;
    }

    public Set<URI> getVisitedUris() {
        return visitedUris;
    }

    public Set<URI> getIgnoredUris() {
        return ignoredUris;
    }

    public Set<String> getIgnoredLinks() {
        return ignoredLinks;
    }
}
