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
package com.dattack.aranea.engine;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

/**
 * @author cvarela
 * @since 0.1
 */
public class ResourceDiscoveryStatus {

    private final ResourceCoordinates resourceCoordinates;
    private final Set<URI> newUris;
    private final Set<URI> alreadyVisited;
    private final Set<URI> ignoredUris;
    private final Set<String> ignoredLinks;

    public ResourceDiscoveryStatus(final ResourceCoordinates resourceCoordinates) {
        this.resourceCoordinates = resourceCoordinates;
        this.newUris = new HashSet<>();
        this.alreadyVisited = new HashSet<>();
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

    public void addAlreadyVisited(final URI uri) {
        if (uri != null) {
            this.alreadyVisited.add(uri);
        }
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

    public ResourceCoordinates getResourceCoordinates() {
        return resourceCoordinates;
    }

    public Set<URI> getAlreadyVisited() {
        return alreadyVisited;
    }

    public boolean isDuplicatedLink(final URI uri) {
        if (uri == null) {
            return true;
        }
        return newUris.contains(uri) || alreadyVisited.contains(uri);
    }
}
