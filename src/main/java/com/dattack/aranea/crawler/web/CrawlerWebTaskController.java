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
import com.dattack.aranea.beans.web.crawler.URINormalizerBean;
import com.dattack.aranea.util.NamedThreadFactory;

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

    private static boolean exclude(final String uri, final List<ExcludeBean> excludeBeanList) {

        for (final ExcludeBean bean : excludeBeanList) {
            if (uri.matches(bean.getRegex())) {
                return true;
            }
        }
        return false;
    }

    public CrawlerWebTaskController(final WebBean sourceBean) {

        this.sourceBean = sourceBean;
        this.repository = new Repository(sourceBean.getRepository());
        this.taskStatus = new CrawlerWebTaskStatus();
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
                submit(new Page(new URI(url)));
            }
        } catch (final URISyntaxException e) {
            log.error(e.getMessage());
        }
    }

    private CrawlerBean getCrawlerBean() {
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
            submitUrisFromDocument(doc, pageInfo);

            final String filename = filenameGenerator.getFilename(pageInfo.getPage().getUri());
            repository.write(filename, doc.html(), pageInfo);

            if (pageInfo.getNewUris().size() == 0 && taskStatus.getPendingUrisCounter() == 0) {
                // TODO: review shutdown conditions
                executor.shutdown();
            }

            log.info("{} new URIs from {}", pageInfo.getNewUris().size(), pageInfo.getPage().getUri().toString());
        } catch (final IOException e) {
            relaunch(pageInfo.getPage());
        }
    }

    private String normalizeUri(final String uri) {

        String normalizedUri = uri;
        for (final LinkNormalizer normalizer : linkNormalizers) {
            normalizedUri = normalizer.normalize(normalizedUri);
        }
        return normalizedUri;
    }

    /**
     * Relaunch a failed page.
     *
     * @param uri
     *            the page to crawl.
     */
    void relaunch(final Page page) {

        final int counter = taskStatus.relaunch(page);
        if (counter < MAX_ERRORS) {
            submit(page);
        }
    }

    private boolean submit(final Page page) {

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

    private void submitUri(final PageInfo pageInfo, final String uriAsText, final URI referer)
            throws URISyntaxException {

        final URI normalizedURI = new URI(normalizeUri(uriAsText));

        if (!pageInfo.isDuplicatedLink(normalizedURI)) {
            if (submit(new Page(normalizedURI, referer))) {
                pageInfo.addNewUri(normalizedURI);
            } else {
                pageInfo.addVisitedUri(normalizedURI);
            }
        }
    }

    private void submitUrisFromDocument(final Document doc, final PageInfo pageInfo) {

        for (final RegionSelectorBean regionSelectorBean : getCrawlerBean().getRegionSelectorList()) {

            if (regionSelectorBean.getSelector() != null) {
                // scan only this area
                final Elements areas = doc.select(regionSelectorBean.getSelector());
                for (final Element element : areas) {
                    submitUrisFromElement(element, regionSelectorBean, pageInfo);
                }
            } else {
                // scan the full document
                submitUrisFromElement(doc, regionSelectorBean, pageInfo);
            }
        }
    }

    private void submitUrisFromElement(final Element element, final RegionSelectorBean domSelectorBean,
            final PageInfo pageInfo) {

        final Elements links = element.select(domSelectorBean.getElement());

        for (final Element link : links) {

            String linkHref = null;
            try {

                linkHref = StringUtils.trimToEmpty(link.attr(domSelectorBean.getAttribute()));
                if (StringUtils.isBlank(linkHref) || pageInfo.getIgnoredLinks().contains(linkHref)
                        || exclude(linkHref, domSelectorBean.getExcludeLinksList())) {
                    continue;
                }

                final URI linkUri = pageInfo.getPage().getUri().resolve(linkHref);
                final String uriAsText = linkUri.toString();

                if (uriAsText.matches(domSelectorBean.getFilter())
                        && !exclude(uriAsText, domSelectorBean.getExcludeUrlList())) {
                    submitUri(pageInfo, uriAsText, pageInfo.getPage().getUri());
                } else {
                    pageInfo.addIgnoredUri(linkUri);
                }

            } catch (final Throwable e) {
                log.warn("Document URL: {}, Child URL: {}, ERROR: {}", pageInfo.getPage().getUri(), linkHref,
                        e.getMessage());
                pageInfo.addIgnoredLink(linkHref);
            }
        }
    }
}