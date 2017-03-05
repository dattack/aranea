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
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dattack.aranea.util.NamedThreadFactory;

/**
 * @author cvarela
 * @since 0.1
 */
public abstract class CrawlerTaskController implements CrawlerTaskControllerMBean {

    private static final Logger log = LoggerFactory.getLogger(CrawlerTaskController.class);

    private final int maxRetries;

    // set of URIs to be visited
    private final Set<ResourceCoordinates> pendingUris;

    // set of URIs already visited
    private final Set<ResourceCoordinates> visitedUris;

    // set of URIs that have ended in an unrecoverable error
    private final Set<ResourceCoordinates> unrecoverableUris;

    // map with the number of retries performed for each URI that ended with a recoverable error
    private final Map<ResourceCoordinates, Short> retriesMap;

    private final ThreadPoolExecutor threadPoolExecutor;

    /**
     * @param maxRetries
     *            the maximum number of retries to be performed
     * @param poolSize
     *            the number of threads to allow in the pool
     * @param threadPrefix
     *            the prefix to named the threads
     */
    public CrawlerTaskController(final int maxRetries, final int poolSize, final String threadPrefix) {
        this.maxRetries = maxRetries;
        this.pendingUris = new HashSet<>();
        this.visitedUris = new HashSet<>();
        this.unrecoverableUris = new HashSet<>();
        this.retriesMap = new HashMap<>();

        this.threadPoolExecutor = new ThreadPoolExecutor(poolSize, poolSize, 1L, TimeUnit.MINUTES,
                new LinkedBlockingQueue<Runnable>(), new NamedThreadFactory(threadPrefix));
    }

    protected abstract Runnable createTask(final ResourceCoordinates resourceCoordinates);

    @Override
    public final int getPendingUrisCounter() {
        return pendingUris.size();
    }

    @Override
    public final int getRecoverableUrisCounter() {
        return retriesMap.size();
    }

    @Override
    public final int getUnrecoverableUrisCounter() {
        return unrecoverableUris.size();
    }

    @Override
    public final int getVisitedUrisCounter() {
        return visitedUris.size();
    }

    private int incrRetriesCounter(final ResourceCoordinates resourceCoordinates) {

        Short counter = retriesMap.get(resourceCoordinates);
        if (counter == null) {
            counter = 1;
        } else {
            counter++;
        }

        retriesMap.put(resourceCoordinates, counter);
        return counter;
    }

    /**
     * A task crawler is complete when there are no resources to be visited, there are no resources that need to be
     * retry access and at least one resource has been visited.
     *
     * @return true if the task is completed
     */
    public final synchronized boolean isCompleted() {
        return pendingUris.isEmpty() && retriesMap.isEmpty() && !visitedUris.isEmpty();
    }

    private boolean isNew(final ResourceCoordinates resourceCoordinates) {
        return !visitedUris.contains(resourceCoordinates) //
                && !pendingUris.contains(resourceCoordinates) //
                && !unrecoverableUris.contains(resourceCoordinates);
    }

    protected abstract boolean isSubmittable(final ResourceCoordinates resourceCoordinates);

    @Override
    public final void logPendingUris() {

        log.info("Number of URIs at pending status: {}", pendingUris.size());
        for (final ResourceCoordinates resource : pendingUris) {
            log.info("Not visited URI: {}", resource.getUri());
        }
    }

    @Override
    public final void logRecoverableUris() {

        log.info("Number of recoverable URIs: {}", retriesMap.size());
        for (final Entry<ResourceCoordinates, Short> entry : retriesMap.entrySet()) {
            log.info("Recoverable URI (#Retries: {}): {}", entry.getValue(), entry.getKey().getUri());
        }
    }

    @Override
    public final void logUnrecoverableUris() {

        log.info("Number of unrecoverable URIs: {}", unrecoverableUris.size());
        for (final ResourceCoordinates resource : unrecoverableUris) {
            log.info("Unrecoverable URI: {}", resource.getUri());
        }
    }

    @Override
    public final void logVisitedUris() {

        log.info("Number of visited URIs: {}", visitedUris.size());
        for (final ResourceCoordinates resource : visitedUris) {
            log.info("Visited URI: {}", resource.getUri());
        }
    }

    /**
     * Checks that the maximum number of retries has already been reached or it is still possible to retry a new access
     * to a resource.
     *
     * @param resourceCoordinates
     *            the resource to retry
     * @return <tt>true</tt> if a retry is possible; <tt>false</tt> otherwise
     */
    public final synchronized boolean retry(final ResourceCoordinates resourceCoordinates) {

        if (unrecoverableUris.contains(resourceCoordinates) || visitedUris.contains(resourceCoordinates)) {
            return false;
        }

        if (incrRetriesCounter(resourceCoordinates) < maxRetries) {
            pendingUris.remove(resourceCoordinates);
            submit(resourceCoordinates);
            return true;
        }

        // max retries reached
        unrecoverable(resourceCoordinates);
        return false;
    }

    public final synchronized boolean submit(final ResourceCoordinates resourceCoordinates) {

        try {
            if (isSubmittable(resourceCoordinates) && isNew(resourceCoordinates)) {

                final Runnable task = createTask(resourceCoordinates);
                if (task != null) {
                    pendingUris.add(resourceCoordinates);
                    threadPoolExecutor.submit(task);
                    return true;
                }
            }
            return false;
        } finally {
            tryShutdown();
        }
    }

    private void tryShutdown() {

        if (!threadPoolExecutor.isShutdown() && isCompleted()) {
            log.info("Shutdown in progress");
            threadPoolExecutor.shutdown();
            log.info("Shutdown completed");
        }
    }

    public final synchronized void unrecoverable(final ResourceCoordinates resourceCoordinates) {
        unrecoverableUris.add(resourceCoordinates);
        retriesMap.remove(resourceCoordinates);
        pendingUris.remove(resourceCoordinates);

        tryShutdown();
    }

    /*
     * Mark a page as visited.
     */
    public final synchronized void visited(final ResourceCoordinates resourceCoordinates) {
        this.visitedUris.add(resourceCoordinates);
        this.pendingUris.remove(resourceCoordinates);
        this.retriesMap.remove(resourceCoordinates);

        tryShutdown();
    }
}
