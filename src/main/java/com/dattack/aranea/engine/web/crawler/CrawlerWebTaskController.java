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
package com.dattack.aranea.engine.web.crawler;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.PropertyConverter;
import org.apache.commons.lang.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dattack.aranea.beans.web.WebBean;
import com.dattack.aranea.beans.web.crawler.CrawlerBean;
import com.dattack.aranea.beans.web.crawler.ExcludeBean;
import com.dattack.aranea.beans.web.crawler.RegionSelectorBean;
import com.dattack.aranea.beans.web.crawler.SeedBean;
import com.dattack.aranea.beans.web.crawler.URINormalizerBean;
import com.dattack.aranea.engine.Context;
import com.dattack.aranea.engine.Page;
import com.dattack.aranea.engine.PageInfo;
import com.dattack.aranea.util.NamedThreadFactory;
import com.dattack.aranea.util.WebTaskUtil;

/**
 * @author cvarela
 * @since 0.1
 */
class CrawlerWebTaskController implements CrawlerWebTaskControllerMBean {

    private static final Logger log = LoggerFactory.getLogger(CrawlerWebTaskController.class);

    private static final int MAX_ERRORS = 3;

    private final WebBean sourceBean;
    private final ThreadPoolExecutor executor;
    private final FilenameGenerator filenameGenerator;
    private final List<LinkNormalizer> linkNormalizers;
    private final CrawlerWebTaskStatus taskStatus;
    private final Repository repository;

    public CrawlerWebTaskController(final WebBean sourceBean) {

        this.sourceBean = sourceBean;
        this.repository = new Repository(Context.get().interpolate(sourceBean.getRepository()));
        this.taskStatus = new CrawlerWebTaskStatus(MAX_ERRORS);
        this.filenameGenerator = new FilenameGenerator(getCrawlerBean().getStorageBean());
        this.executor = new ThreadPoolExecutor(getCrawlerBean().getThreadPoolSize(),
                getCrawlerBean().getThreadPoolSize(), 1L, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>(),
                new NamedThreadFactory(sourceBean.getId()));

        this.linkNormalizers = new ArrayList<>();
        for (final URINormalizerBean lnc : getCrawlerBean().getNormalizerList()) {
            linkNormalizers.add(new LinkNormalizer(lnc));
        }
    }

    public void execute() {
        try {
            for (final String url : getCrawlerBean().getHomeList()) {
                submit(new Page(new URI(Context.get().interpolate(url))));
            }
        } catch (final URISyntaxException e) {
            log.error(e.getMessage());
        }
    }

    protected CrawlerBean getCrawlerBean() {
        return getSourceBean().getCrawler();
    }

    @Override
    public Set<String> getErrorUris(final int start, final int offset) {

        return getUriSubset(taskStatus.getErrorUris(), start, offset);
    }

    @Override
    public int getErrorUrisCounter() {
        return taskStatus.getErrorUrisCounter();
    }

    @Override
    public Set<String> getPendingUris(final int start, final int offset) {

        return getUriSubset(taskStatus.getPendingUris(), start, offset);
    }

    @Override
    public int getPendingUrisCounter() {
        return taskStatus.getPendingUrisCounter();
    }

    WebBean getSourceBean() {
        return sourceBean;
    }

    int getTimeout() {
        return getCrawlerBean().getTimeout();
    }

    private Set<String> getUriSubset(final Set<Page> pages, final int start, final int offset) {

        final List<Page> pageList = new ArrayList<>(pages);

        final Set<String> set = new HashSet<>();
        int index = start;
        while (index < pages.size() && index < start + offset) {
            set.add(pageList.get(index++).getUri().toString());
        }
        return set;
    }

    @Override
    public Set<String> getVisitedUris(final int start, final int offset) {

        return getUriSubset(taskStatus.getVisitedUris(), start, offset);
    }

    @Override
    public int getVisitedUrisCounter() {
        return taskStatus.getVisitedUrisCounter();
    }

    void handle(final PageInfo pageInfo, final Document doc) {

        try {
            taskStatus.registerAsVisited(pageInfo.getPage());

            final String filename = filenameGenerator.getFilename(pageInfo.getPage().getUri());
            repository.write(filename, doc.html(), pageInfo);

            tryShutdown();

            log.info("{} new URIs from {}", pageInfo.getNewUris().size(), pageInfo.getPage().getUri().toString());
        } catch (final IOException e) {
            relaunch(pageInfo);
        }
    }

    protected String normalizeUri(final String uri) {

        String normalizedUri = uri;
        for (final LinkNormalizer normalizer : linkNormalizers) {
            normalizedUri = normalizer.normalize(normalizedUri);
        }
        return normalizedUri;
    }

    private void tryShutdown() {
        if (taskStatus.getPendingUrisCounter() == 0) {
            executor.shutdown();
        }
    }

    void fail(final PageInfo pageInfo) {

        taskStatus.fail(pageInfo);
        tryShutdown();
    }

    /**
     * Relaunch a failed page.
     *
     * @param uri
     *            the page to crawl.
     */
    void relaunch(final PageInfo pageInfo) {

        final boolean relaunch = taskStatus.relaunch(pageInfo);
        if (relaunch) {
            submit(pageInfo.getPage());
        } else {
            tryShutdown();
        }
    }

    protected boolean submit(final Page page) {

        try {
            if (taskStatus.submit(page)) {
                executor.submit(new CrawlerWebTask(page, this));
                return true;
            }
            return false;
        } catch (final Throwable t) {
            t.printStackTrace();
            return false;
        }
    }

}