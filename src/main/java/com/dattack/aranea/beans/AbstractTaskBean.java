/*
 * Copyright (c) 2016, The Dattack team (http://www.dattack.com)
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
package com.dattack.aranea.beans;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * @author cvarela
 * @since 0.1
 */
public abstract class AbstractTaskBean {

    private static final int DEFAULT_THREAD_POOL_SIZE = 3;
    private static final int DEFAULT_TIMEOUT = 30000;
    
    @XmlAttribute(name = XmlTokens.ID, required = true)
    private String id;

    @XmlAttribute(name = XmlTokens.REPOSITORY, required = true)
    private String repository;
    
    @XmlAttribute(name = XmlTokens.HOME, required = true)
    private String home;

    @XmlAttribute(name = XmlTokens.LATENCY, required = true)
    private long latency;
    
    @XmlAttribute(name = XmlTokens.THREAD_POOL_SIZE, required = false)
    private int threadPoolSize;

    @XmlAttribute(name = XmlTokens.TIMEOUT, required = false)
    private long timeout;

    @XmlAttribute(name = XmlTokens.USER_AGENT, required = false)
    private String userAgent;
    
    public AbstractTaskBean() {
        this.timeout = DEFAULT_TIMEOUT;
        this.threadPoolSize = DEFAULT_THREAD_POOL_SIZE;
        this.repository = ".";
    }
    
    public final String getId() {
        return id;
    }
    
    public final String getHome() {
        return home;
    }

    public final long getLatency() {
        return latency;
    }

    public final String getRepository() {
        return repository;
    }

    public final int getThreadPoolSize() {
        return threadPoolSize;
    }

    public final long getTimeout() {
        return timeout;
    }

    public final String getUserAgent() {
        return userAgent;
    }
}
