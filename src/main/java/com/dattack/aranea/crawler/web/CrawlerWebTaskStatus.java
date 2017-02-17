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

/**
 * @author cvarela
 * @since 0.1
 */
class CrawlerWebTaskStatus {

    private final Set<Page> pendingUris;
    private final Set<Page> visitedUris;
    private final Map<Page, Short> errorCounter;

    protected CrawlerWebTaskStatus() {
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

    void registerAsVisited(final Page page) {
        this.visitedUris.add(page);
        this.pendingUris.remove(page);
        errorCounter.remove(page);
    }

    int relaunch(final Page page) {

        int counter = incrErrorCounter(page);
        pendingUris.remove(page);
        return counter;
    }

    boolean submit(final Page uri) {

        if (!visitedUris.contains(uri) && !pendingUris.contains(uri)) {
            pendingUris.add(uri);
            return true;
        }
        return false;
    }

    public int getErrorUrisCounter() {
        return errorCounter.size();
    }

    public int getPendingUrisCounter() {
        return pendingUris.size();
    }

    public int getVisitedUrisCounter() {
        return visitedUris.size();
    }
}
