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
package com.dattack.aranea.engine.rest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import com.dattack.aranea.beans.appender.AbstractAppender;
import com.dattack.aranea.beans.appender.FileAppender;
import com.dattack.aranea.beans.rest.ResourceBean;
import com.dattack.aranea.beans.rest.RestBean;
import com.dattack.aranea.engine.Context;
import com.dattack.aranea.engine.CrawlerTaskStatus;
import com.dattack.aranea.engine.Page;
import com.dattack.aranea.engine.PageInfo;
import com.dattack.aranea.util.NamedThreadFactory;
import com.dattack.jtoolbox.commons.configuration.ConfigurationUtil;

/**
 * @author cvarela
 *
 */
public class CrawlerRestTaskController {

    private final RestBean restBean;
    private final Map<String, OutputStream> outputMapping;
    private final ThreadPoolExecutor executor;
    private final CrawlerTaskStatus crawlerStatus;
    private final Set<Object> resourceIdList;

    public CrawlerRestTaskController(final RestBean restBean) {
        this.restBean = restBean;
        this.outputMapping = new HashMap<>();
        this.crawlerStatus = new CrawlerTaskStatus(3);
        this.executor = new ThreadPoolExecutor(restBean.getCrawlerBean().getThreadPoolSize(),
                restBean.getCrawlerBean().getThreadPoolSize(), 1L, TimeUnit.MINUTES,
                new LinkedBlockingQueue<Runnable>(), new NamedThreadFactory(restBean.getId()));
        this.resourceIdList = new HashSet<>();
    }

    public void execute() {

        for (final String url : restBean.getCrawlerBean().getEntryPointList()) {
            try {
                submit(new Page(new URI(Context.get().interpolate(url))));
            } catch (URISyntaxException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private void tryShutdown() {
        if (crawlerStatus.getPendingUrisCounter() == 0) {
            executor.shutdown();
        }
    }

    public synchronized OutputStream getOutputStream(final String path) throws FileNotFoundException {

        OutputStream outputStream = outputMapping.get(path);
        if (outputStream == null) {
            outputStream = new FileOutputStream(new File(path));
            outputMapping.put(path, outputStream);
        }
        return outputStream;
    }

    public ResourceBean lookupResource(final String uri) {

        for (final ResourceBean item : restBean.getResourceBeanList()) {
            final String pattern = Context.get().interpolate(item.getRegex());
            if (uri.matches(pattern)) {
                return item;
            }
        }

        return null;
    }

    void fail(final PageInfo pageInfo) {

        crawlerStatus.fail(pageInfo);
        tryShutdown();
    }

    /**
     * Relaunch a failed page.
     *
     * @param uri
     *            the page to crawl.
     */
    void relaunch(final PageInfo pageInfo) {

        final boolean relaunch = crawlerStatus.relaunch(pageInfo);
        if (relaunch) {
            submit(pageInfo.getPage());
        } else {
            tryShutdown();
        }
    }

    private void notifyAppenders(final BaseConfiguration configuration, final List<AbstractAppender> appenderList) {

        for (final AbstractAppender appender : appenderList) {

            if (appender instanceof FileAppender) {
                final FileAppender fileAppender = (FileAppender) appender;
                try {
                    final StringBuilder sb = new StringBuilder()
                            .append(ConfigurationUtil.interpolate(fileAppender.getPattern(), configuration))
                            .append("\n");
                    IOUtils.write(sb.toString(),
                            getOutputStream(Context.get().interpolate(fileAppender.getFilename())));

                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void handleResources(final ResourceBean resourceBean, final List<Map<String, Object>> resourceList) {

        for (Map<String, Object> resource : resourceList) {

            final BaseConfiguration configuration = new BaseConfiguration();

            boolean skipResource = false;
            for (final Entry<String, Object> item : resource.entrySet()) {
                if (item.getKey().equalsIgnoreCase("_id")) {
                    synchronized (resourceIdList) {
                        if (resourceIdList.contains(item.getValue())) {
                            skipResource = true;
                            break;
                        } else {
                            resourceIdList.add(item.getValue());
                        }
                    }
                }

                configuration.setProperty(item.getKey(),
                        StringUtils.trimToEmpty(ObjectUtils.toString(item.getValue())));
            }

            if (!skipResource) {
                notifyAppenders(configuration, resourceBean.getAppenders().getAppenderList());
            }
        }
    }

    void handle(final PageInfo pageInfo, List<Map<String, Object>> resources) {

        try {
            crawlerStatus.registerAsVisited(pageInfo.getPage());

            final ResourceBean resourceBean = lookupResource(pageInfo.getPage().getUri().toString());
            if (resourceBean != null && resourceBean.hasAppenders()) {
                handleResources(resourceBean, resources);
            }

            tryShutdown();

        } catch (final Exception e) {
            relaunch(pageInfo);
        }
    }

    public void submit(final Page page) {
        try {

            final ResourceBean resourceBean = lookupResource(page.getUri().toString());

            if (resourceBean != null) {
                if (crawlerStatus.submit(page)) {
                    executor.submit(new CrawlerRestTask(page, this, resourceBean));
                }
            }

        } finally {
            tryShutdown();
        }
    }
}
