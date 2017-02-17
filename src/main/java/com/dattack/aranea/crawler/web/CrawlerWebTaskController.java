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

    public CrawlerWebTaskController(final WebBean sourceBean) {

        this.sourceBean = sourceBean;
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
            PageLinkParseStatus pageStatus = new PageLinkParseStatus(page);

            saveDocument(page, doc, pageStatus);
            submitUrisFromDocument(doc, page, pageStatus);

            taskStatus.registerAsVisited(page);

            if (pageStatus.getNewUris().size() == 0 && taskStatus.getPendingUrisCounter() == 0) {
                executor.shutdown();
            }

            log.info("{} new URIs from {}", pageStatus.getNewUris().size(), page.getUri().toString());
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

    private void saveDocument(final Page page, final Document doc, final PageLinkParseStatus pageStatus)
            throws IOException {

        try {
            pageStatus.setFilename(filenameGenerator.getFilename(page.getUri()));
            File file = new File(getSourceBean().getRepository(), pageStatus.getFilename());
            FileUtils.writeStringToFile(file, doc.html());
            log.debug("{} stored as {}", page.getUri().toString(), pageStatus.getFilename());
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
            return false;
        }
    }

    private void submitUrisFromDocument(final Document doc, final Page page, final PageLinkParseStatus pageStatus) {

        for (RegionSelectorBean regionSelectorBean : getCrawlerBean().getRegionSelectorList()) {

            if (regionSelectorBean.getSelector() != null) {
                // scan only this area
                Elements areas = doc.select(regionSelectorBean.getSelector());
                for (Element element : areas) {
                    submitUrisFromElement(element, page, regionSelectorBean, pageStatus);
                }
            } else {
                // scan the full document
                submitUrisFromElement(doc, page, regionSelectorBean, pageStatus);
            }
        }
    }

    private void submitUrisFromElement(final Element element, final Page page, final RegionSelectorBean domSelectorBean,
            final PageLinkParseStatus pageStatus) {

        Elements links = element.select(domSelectorBean.getElement());

        for (Element link : links) {

            String uriAsText = null;
            try {

                String linkHref = link.attr(domSelectorBean.getAttribute());
                if (StringUtils.isBlank(linkHref) || (exclude(linkHref, domSelectorBean.getExcludeList()))) {
                    continue;
                }

                URI linkUri = page.getUri().resolve(linkHref);
                uriAsText = linkUri.toString();

                if (uriAsText.matches(domSelectorBean.getFilter())
                        && !exclude(uriAsText, domSelectorBean.getExcludeList())) {
                    submitUri(pageStatus, uriAsText, page.getUri());
                } else {
                    pageStatus.addIgnoredUri(linkUri);
                }

            } catch (Throwable e) {
                log.warn("Document URL: {}, Child URL: {}, ERROR: {}", page.getUri(), uriAsText, e.getMessage());
            }
        }
    }

    private void submitUri(final PageLinkParseStatus pageStatus, final String uriAsText, final URI referer)
            throws URISyntaxException {

        URI normalizedURI = new URI(normalizeUri(uriAsText));

        if (!pageStatus.isDuplicatedLink(normalizedURI)) {
            if (submit(new Page(normalizedURI, referer))) {
                pageStatus.addNewUri(normalizedURI);
            } else {
                pageStatus.addVisitedUri(normalizedURI);
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

    private class PageLinkParseStatus {

        private final Page page;
        private String filename;
        private final Set<URI> newUris;
        private final Set<URI> visitedUris;
        private final Set<URI> ignoredUris;

        public PageLinkParseStatus(final Page page) {
            this.page = page;
            this.newUris = new HashSet<>();
            this.visitedUris = new HashSet<>();
            this.ignoredUris = new HashSet<>();
        }

        public void setFilename(final String filename) {
            this.filename = filename;
        }

        public String getFilename() {
            return filename;
        }

        public boolean isDuplicatedLink(final URI uri) {
            return newUris.contains(uri) || visitedUris.contains(uri);
        }

        public void addNewUri(final URI uri) {
            this.newUris.add(uri);
        }

        public void addVisitedUri(final URI uri) {
            this.visitedUris.add(uri);
        }

        public void addIgnoredUri(final URI uri) {
            this.ignoredUris.add(uri);
        }

        public Page getPage() {
            return page;
        }

        public Set<URI> getNewUris() {
            return newUris;
        }

        public Set<URI> getVisitedUris() {
            return visitedUris;
        }

        public Set<URI> getIgnoredUris() {
            return ignoredUris;
        }
    }
}