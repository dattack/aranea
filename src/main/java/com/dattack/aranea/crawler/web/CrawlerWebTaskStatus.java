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
package com.dattack.aranea.crawler.web;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpStatus;

/**
 * @author cvarela
 * @since 0.1
 */
class CrawlerWebTaskStatus {

    private final int maxErrors;
    private final Set<Page> pendingUris;
    private final Set<Page> visitedUris;
    private final Map<Page, Short> errorCounter;

    protected CrawlerWebTaskStatus(final int maxErrors) {
        this.maxErrors = maxErrors;
        this.pendingUris = new HashSet<Page>();
        this.visitedUris = new HashSet<Page>();
        this.errorCounter = new HashMap<Page, Short>();
    }

    private int incrErrorCounter(final Page uri) {

        Short counter = errorCounter.get(uri);
        if (counter == null) {
            counter = 1;
        } else {
            counter++;
        }

        errorCounter.put(uri, counter);
        return counter;
    }

    /*
     * Mark a page as visited.
     */
    void registerAsVisited(final Page page) {
        this.visitedUris.add(page);
        this.pendingUris.remove(page);
        errorCounter.remove(page);
    }

    void fail(final PageInfo pageInfo) {
        errorCounter.remove(pageInfo.getPage());
        pendingUris.remove(pageInfo.getPage());
    }

    boolean relaunch(final PageInfo pageInfo) {

        boolean relaunch = pageInfo.getStatusCode() != HttpStatus.SC_NOT_FOUND;

        if (relaunch) {
            int counter = incrErrorCounter(pageInfo.getPage());
            relaunch &= counter < maxErrors;
        }

        if (!relaunch) {
            fail(pageInfo);
        } else {
            pendingUris.remove(pageInfo.getPage());
        }

        return relaunch;
    }

    boolean submit(final Page uri) {

        if (!visitedUris.contains(uri) && !pendingUris.contains(uri)) {
            pendingUris.add(uri);
            return true;
        }
        return false;
    }

    public Set<Page> getErrorUris() {
        return errorCounter.keySet();
    }

    public int getErrorUrisCounter() {
        return errorCounter.size();
    }

    public Set<Page> getPendingUris() {
        return pendingUris;
    }

    public int getPendingUrisCounter() {
        return pendingUris.size();
    }

    public Set<Page> getVisitedUris() {
        return visitedUris;
    }

    public int getVisitedUrisCounter() {
        return visitedUris.size();
    }
}
