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
package com.dattack.aranea.beans.web.crawler;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import com.dattack.aranea.beans.XmlTokens;

/**
 * @author cvarela
 * @since 0.1
 */
public class CrawlerBean {

    private static final int DEFAULT_THREAD_POOL_SIZE = 3;
    private static final int DEFAULT_TIMEOUT = 30000;

    @XmlElement(name = XmlTokens.REGION, required = true)
    private final List<RegionSelectorBean> regionSelectorList;

    @XmlElement(name = XmlTokens.HOME, required = true)
    private final List<String> homeList;

    @XmlAttribute(name = XmlTokens.LATENCY, required = true)
    private long latency;

    @XmlElement(name = XmlTokens.NORMALIZER, required = false)
    private final List<URINormalizerBean> normalizerList;

    @XmlElement(name = XmlTokens.STORAGE, required = true)
    private StorageBean storageBean;

    @XmlAttribute(name = XmlTokens.THREAD_POOL_SIZE, required = false)
    private final int threadPoolSize;

    @XmlAttribute(name = XmlTokens.TIMEOUT, required = false)
    private final int timeout;

    @XmlAttribute(name = XmlTokens.USER_AGENT, required = false)
    private String userAgent;

    public CrawlerBean() {
        this.regionSelectorList = new ArrayList<>();
        this.homeList = new ArrayList<>();
        this.normalizerList = new ArrayList<>();
        this.timeout = DEFAULT_TIMEOUT;
        this.threadPoolSize = DEFAULT_THREAD_POOL_SIZE;
    }

    public List<RegionSelectorBean> getRegionSelectorList() {
        return regionSelectorList;
    }

    public List<String> getHomeList() {
        return homeList;
    }

    public long getLatency() {
        return latency;
    }

    public List<URINormalizerBean> getNormalizerList() {
        return normalizerList;
    }

    public StorageBean getStorageBean() {
        return storageBean;
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

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("CrawlerBean [home=").append(homeList) //
        .append(", latency=").append(latency) //
        .append(", regionSelectorList=").append(regionSelectorList) //
        .append(", uriNormalizerList=").append(normalizerList).append("]");
        return builder.toString();
    }
}
