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
package com.dattack.aranea.beans.rest;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.dattack.aranea.beans.XmlTokens;

/**
 * @author cvarela
 * @since 0.1
 */
@XmlType(namespace = "rest")
public class CrawlerBean {

    private static final int DEFAULT_THREAD_POOL_SIZE = 3;
    private static final int DEFAULT_TIMEOUT = 30000;

    @XmlElement(name = XmlTokens.ENTRY_POINT, required = true)
    private final List<String> entryPointList;

    @XmlAttribute(name = XmlTokens.LATENCY, required = true)
    private long latency;

    @XmlAttribute(name = XmlTokens.THREAD_POOL_SIZE, required = false)
    private final int threadPoolSize;

    @XmlAttribute(name = XmlTokens.TIMEOUT, required = false)
    private final int timeout;

    @XmlAttribute(name = XmlTokens.USER_AGENT, required = false)
    private String userAgent;

    public CrawlerBean() {
        this.entryPointList = new ArrayList<>();
        this.timeout = DEFAULT_TIMEOUT;
        this.threadPoolSize = DEFAULT_THREAD_POOL_SIZE;
    }

    public List<String> getEntryPointList() {
        return entryPointList;
    }

    public long getLatency() {
        return latency;
    }

    public int getThreadPoolSize() {
        return threadPoolSize;
    }

    public int getTimeout() {
        return timeout;
    }

    public String getUserAgent() {
        return userAgent;
    }
}
