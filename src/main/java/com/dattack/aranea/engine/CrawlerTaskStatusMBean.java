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

/**
 * MBean that exposes the internal status of the crawling process.
 *
 * @author cvarela
 * @since 0.1
 */
public interface CrawlerTaskStatusMBean {

    /**
     * Returns the number of URIs in 'pending' state.
     *
     * @return the number of URIs in 'pending' state
     */
    int getPendingUrisCounter();

    /**
     * Returns the number of URIs that ended with an error but a retry is still possible.
     *
     * @return the number of URIs that ended with an error but a retry is still possible
     */
    int getRecoverableUrisCounter();

    /**
     * Returns the number of URIs that ended with an fatal error and a retry isn't possible.
     *
     * @return the number of URIs that ended with an fatal error and a retry isn't possible
     */
    int getUnrecoverableUrisCounter();

    /**
     * Returns the number of URIs visited and completed successfully.
     *
     * @return the number of URIs visited and completed successfully
     */
    int getVisitedUrisCounter();

    /**
     * Logs the URIs in 'pending' state.
     */
    void logPendingUris();

    /**
     * Logs the URIs that ended with an error but a retry is still possible.
     */
    void logRecoverableUris();

    /**
     * Logs the URIs that ended with an fatal error and a retry isn't possible.
     */
    void logUnrecoverableUris();

    /**
     * Logs the URIs visited and completed successfully.
     */
    void logVisitedUris();
}
