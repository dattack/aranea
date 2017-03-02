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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author cvarela
 * @since 0.1
 */
public class CrawlerTaskStatus implements CrawlerTaskStatusMBean {

    private final int maxErrors;
    private final Set<ResourceCoordinates> pendingUris;
    private final Set<ResourceCoordinates> visitedUris;
    private final Set<ResourceCoordinates> failedUris;
    private final Map<ResourceCoordinates, Short> errorCounter;

    public CrawlerTaskStatus(final int maxErrors) {
        this.maxErrors = maxErrors;
        this.pendingUris = new HashSet<ResourceCoordinates>();
        this.visitedUris = new HashSet<ResourceCoordinates>();
        this.failedUris = new HashSet<ResourceCoordinates>();
        this.errorCounter = new HashMap<ResourceCoordinates, Short>();
    }

    private int incrErrorCounter(final ResourceCoordinates resourceCoordinates) {

        Short counter = errorCounter.get(resourceCoordinates);
        if (counter == null) {
            counter = 1;
        } else {
            counter++;
        }

        errorCounter.put(resourceCoordinates, counter);
        return counter;
    }

    /*
     * Mark a page as visited.
     */
    public synchronized void registerAsVisited(final ResourceCoordinates resourceCoordinates) {
        this.visitedUris.add(resourceCoordinates);
        this.pendingUris.remove(resourceCoordinates);
        this.errorCounter.remove(resourceCoordinates);
    }

    public synchronized void fail(final ResourceCoordinates resourceCoordinates) {
        failedUris.add(resourceCoordinates);
        errorCounter.remove(resourceCoordinates);
        pendingUris.remove(resourceCoordinates);
    }

    public synchronized boolean relaunch(final ResourceCoordinates resourceCoordinates) {

        int counter = incrErrorCounter(resourceCoordinates);
        boolean relaunch = counter < maxErrors;

        if (!relaunch) {
            fail(resourceCoordinates);
        } else {
            pendingUris.remove(resourceCoordinates);
        }

        return relaunch;
    }

    public synchronized boolean isCompleted() {
        return pendingUris.isEmpty() && errorCounter.isEmpty() && !visitedUris.isEmpty();
    }

    public synchronized boolean submit(final ResourceCoordinates resourceCoordinates) {

        if (isNew(resourceCoordinates)) {
            pendingUris.add(resourceCoordinates);
            return true;
        }
        return false;
    }

    private boolean isNew(final ResourceCoordinates resourceCoordinates) {
        return !visitedUris.contains(resourceCoordinates) //
                && !pendingUris.contains(resourceCoordinates) //
                && !failedUris.contains(resourceCoordinates);
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

    public int getFailedUrisCounter() {
        return failedUris.size();
    }
}
