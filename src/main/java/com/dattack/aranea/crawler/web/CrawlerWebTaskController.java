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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dattack.aranea.beans.web.WebBean;
import com.dattack.aranea.beans.web.crawler.CrawlerBean;
import com.dattack.aranea.beans.web.crawler.RegionSelectorBean;
import com.dattack.aranea.beans.web.crawler.ExcludeBean;
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
    
    
    public CrawlerWebTaskController(final WebBean sourceBean) {

        this.sourceBean = sourceBean;
        this.repository = new Repository(sourceBean.getRepository());
        this.taskStatus = new CrawlerWebTaskStatus();
        this.filenameGenerator = new FilenameGenerator(getCrawlerBean().getStorageBean());
        this.executor = new ThreadPoolExecutor(getCrawlerBean().getThreadPoolSize(),
                getCrawlerBean().getThreadPoolSize(), 1L, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>(),
                new NamedThreadFactory(sourceBean.getId()));

        this.linkNormalizers = new ArrayList<>();
        for (URINormalizerBean lnc : getCrawlerBean().getNormalizerList()) {
            linkNormalizers.add(new LinkNormalizer(lnc));
        }
    }

    WebBean getSourceBean() {
        return sourceBean;
    }

    private CrawlerBean getCrawlerBean() {
        return getSourceBean().getCrawler();
    }

    private static boolean exclude(final String uri, final List<ExcludeBean> excludeBeanList) {

        for (ExcludeBean bean : excludeBeanList) {
            if (uri.matches(bean.getRegex())) {
                return true;
            }
        }
        return false;
    }

    public void execute() {
        try {
            for (final String url : getCrawlerBean().getHomeList()) {
                submit(new Page(new URI(url)));
            }
        } catch (URISyntaxException e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public int getErrorUrisCounter() {
        return taskStatus.getErrorUrisCounter();
    }

    public Set<String> getErrorUris(final int start, final int offset) {

        return getUriSubset(taskStatus.getErrorUris(), start, offset);
    }

    public Set<String> getPendingUris(final int start, final int offset) {

        return getUriSubset(taskStatus.getPendingUris(), start, offset);
    }

    public Set<String> getVisitedUris(final int start, final int offset) {

        return getUriSubset(taskStatus.getVisitedUris(), start, offset);
    }

    private Set<String> getUriSubset(final Set<Page> pages, final int start, final int offset) {

        List<Page> pageList = new ArrayList<>(pages);

        Set<String> set = new HashSet<>();
        int index = start;
        while (index < pages.size() && index < start + offset) {
            set.add(pageList.get(index++).getUri().toString());
        }
        return set;
    }

    @Override
    public int getPendingUrisCounter() {
        return taskStatus.getPendingUrisCounter();
    }

    int getTimeout() {
        return getCrawlerBean().getTimeout();
    }

    @Override
    public int getVisitedUrisCounter() {
        return taskStatus.getVisitedUrisCounter();
    }

    void handle(final Page page, final Document doc) {

        try {
            PageInfo pageInfo = new PageInfo(page);

            saveDocument(doc, pageInfo);
            submitUrisFromDocument(doc, pageInfo);

            taskStatus.registerAsVisited(page);

            if (pageInfo.getNewUris().size() == 0 && taskStatus.getPendingUrisCounter() == 0) {
                // TODO: review shutdown conditions
                executor.shutdown();
            }

            log.info("{} new URIs from {}", pageInfo.getNewUris().size(), page.getUri().toString());
        } catch (IOException e) {
            relaunch(page);
        }
    }

    /**
     * Relaunch a failed page.
     *
     * @param uri
     *            the page to crawl.
     */
    void relaunch(final Page page) {

        int counter = taskStatus.relaunch(page);
        if (counter < MAX_ERRORS) {
            submit(page);
        }
    }

    private void saveDocument(final Document doc, final PageInfo pageInfo)
            throws IOException {

        try {
            String filename = filenameGenerator.getFilename(pageInfo.getPage().getUri());
            repository.write(filename, doc.html(), pageInfo);
            
            log.debug("{} stored as {}", pageInfo.getPage().getUri().toString(), filename);
        } catch (UnknownFilenameException e) {
            log.trace(e.getMessage());
        }
    }

    private boolean submit(final Page page) {

        try {
            if (taskStatus.submit(page)) {
                executor.submit(new CrawlerWebTask(page, this));
                return true;
            }
            return false;
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
    }

    private void submitUrisFromDocument(final Document doc, final PageInfo pageInfo) {

        for (RegionSelectorBean regionSelectorBean : getCrawlerBean().getRegionSelectorList()) {

            if (regionSelectorBean.getSelector() != null) {
                // scan only this area
                Elements areas = doc.select(regionSelectorBean.getSelector());
                for (Element element : areas) {
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

        Elements links = element.select(domSelectorBean.getElement());

        for (Element link : links) {

            String linkHref = null;
            try {

                linkHref = StringUtils.trimToEmpty(link.attr(domSelectorBean.getAttribute()));
                if (StringUtils.isBlank(linkHref) || pageInfo.getIgnoredLinks().contains(linkHref)
                        || (exclude(linkHref, domSelectorBean.getExcludeLinksList()))) {
                    continue;
                }

                URI linkUri = pageInfo.getPage().getUri().resolve(linkHref);
                String uriAsText = linkUri.toString();

                if (uriAsText.matches(domSelectorBean.getFilter())
                        && !exclude(uriAsText, domSelectorBean.getExcludeUrlList())) {
                    submitUri(pageInfo, uriAsText, pageInfo.getPage().getUri());
                } else {
                    pageInfo.addIgnoredUri(linkUri);
                }

            } catch (Throwable e) {
                log.warn("Document URL: {}, Child URL: {}, ERROR: {}", pageInfo.getPage().getUri(), linkHref, e.getMessage());
                pageInfo.addIgnoredLink(linkHref);
            }
        }
    }

    private void submitUri(final PageInfo pageInfo, final String uriAsText, final URI referer)
            throws URISyntaxException {

        URI normalizedURI = new URI(normalizeUri(uriAsText));

        if (!pageInfo.isDuplicatedLink(normalizedURI)) {
            if (submit(new Page(normalizedURI, referer))) {
                pageInfo.addNewUri(normalizedURI);
            } else {
                pageInfo.addVisitedUri(normalizedURI);
            }
        }
    }

    private String normalizeUri(final String uri) {

        String normalizedUri = uri;
        for (LinkNormalizer normalizer : linkNormalizers) {
            normalizedUri = normalizer.normalize(normalizedUri);
        }
        return normalizedUri;
    }
}