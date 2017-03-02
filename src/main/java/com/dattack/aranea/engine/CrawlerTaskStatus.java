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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
    private final Map<ResourceCoordinates, Short> errorCounter;

    public CrawlerTaskStatus(final int maxErrors) {
        this.maxErrors = maxErrors;
        this.pendingUris = new HashSet<ResourceCoordinates>();
        this.visitedUris = new HashSet<ResourceCoordinates>();
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
    public void registerAsVisited(final ResourceCoordinates resourceCoordinates) {
        this.visitedUris.add(resourceCoordinates);
        this.pendingUris.remove(resourceCoordinates);
        errorCounter.remove(resourceCoordinates);
    }

    public void fail(final ResourceCoordinates resourceCoordinates) {
        errorCounter.remove(resourceCoordinates);
        pendingUris.remove(resourceCoordinates);
    }

    public boolean relaunch(final ResourceCoordinates resourceCoordinates) {

        int counter = incrErrorCounter(resourceCoordinates);
        boolean relaunch = counter < maxErrors;

        if (!relaunch) {
            fail(resourceCoordinates);
        } else {
            pendingUris.remove(resourceCoordinates);
        }

        return relaunch;
    }

    public boolean submit(final ResourceCoordinates resourceCoordinates) {

        if (!visitedUris.contains(resourceCoordinates) && !pendingUris.contains(resourceCoordinates)) {
            pendingUris.add(resourceCoordinates);
            return true;
        }
        return false;
    }

    public Set<ResourceCoordinates> getErrorUris() {
        return errorCounter.keySet();
    }

    public int getErrorUrisCounter() {
        return errorCounter.size();
    }

    public Set<ResourceCoordinates> getPendingUris() {
        return pendingUris;
    }

    public int getPendingUrisCounter() {
        return pendingUris.size();
    }

    public Set<ResourceCoordinates> getVisitedUris() {
        return visitedUris;
    }

    public int getVisitedUrisCounter() {
        return visitedUris.size();
    }
    
    @Override
    public Set<String> getVisitedUris(final int start, final int offset) {

        return getUriSubset(getVisitedUris(), start, offset);
    }

    @Override
    public Set<String> getErrorUris(final int start, final int offset) {

        return getUriSubset(getErrorUris(), start, offset);
    }

    @Override
    public Set<String> getPendingUris(final int start, final int offset) {

        return getUriSubset(getPendingUris(), start, offset);
    }
    
    private Set<String> getUriSubset(final Set<ResourceCoordinates> resources, final int start, final int offset) {

        final List<ResourceCoordinates> pageList = new ArrayList<>(resources);

        final Set<String> set = new HashSet<>();
        int index = start;
        while (index < resources.size() && index < start + offset) {
            set.add(pageList.get(index++).getUri().toString());
        }
        return set;
    }
}
