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
import java.util.List;

import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dattack.aranea.beans.jobs.Job;
import com.dattack.aranea.beans.web.WebBean;
import com.dattack.aranea.beans.web.crawler.CrawlerBean;
import com.dattack.aranea.beans.web.crawler.URINormalizerBean;
import com.dattack.aranea.engine.CrawlerTaskController;
import com.dattack.aranea.engine.ResourceCoordinates;
import com.dattack.aranea.engine.ResourceDiscoveryStatus;
import com.dattack.aranea.util.JmxUtil;

/**
 * @author cvarela
 * @since 0.1
 */
public class CrawlerWebTaskController extends CrawlerTaskController {

    private static final Logger log = LoggerFactory.getLogger(CrawlerWebTaskController.class);

    private static final int MAX_ERRORS = 3;

    private final CrawlerBean crawlerBean;
    private final List<LinkNormalizer> linkNormalizers;
    private final Repository repository;

    public CrawlerWebTaskController(final WebBean webBean, final Job job) {

        super(MAX_ERRORS, webBean.getCrawler().getThreadPoolSize(), webBean.getId(), job);

        this.crawlerBean = webBean.getCrawler();
        this.repository = new Repository(getCrawlerBean().getStorageBean(), getContext());
        this.linkNormalizers = createLinkNormalizerList();
        
        JmxUtil.registerMBean(this,
                JmxUtil.createObjectName(String.format("com.dattack.aranea.web:type=%s,name=%s", //
                        this.getClass().getSimpleName(), //
                        webBean.getId())));
    }

    private List<LinkNormalizer> createLinkNormalizerList() {
        ArrayList<LinkNormalizer> list = new ArrayList<>();
        for (final URINormalizerBean lnc : getCrawlerBean().getNormalizerList()) {
            list.add(new LinkNormalizer(lnc));
        }
        return list;
    }

    @Override
    protected Runnable createTask(final ResourceCoordinates resourceCoordinates) {
        return new CrawlerWebTask(resourceCoordinates, this);
    }

    public void execute() {
        try {
            for (final String url : getCrawlerBean().getHomeList()) {

                final ResourceCoordinates resourceCoordinates = new ResourceCoordinates(
                        new URI(getContext().interpolate(url)));
                submit(resourceCoordinates);
            }
        } catch (final URISyntaxException e) {
            log.error(e.getMessage());
        }
    }

    protected CrawlerBean getCrawlerBean() {
        return crawlerBean;
    }

    int getTimeout() {
        return getCrawlerBean().getTimeout();
    }

    void handle(final ResourceDiscoveryStatus resourceDiscoveryStatus, final Document doc) {

        try {
            visited(resourceDiscoveryStatus.getResourceCoordinates());

            repository.write(resourceDiscoveryStatus, doc.html());

            log.info("{} new URIs from {}", resourceDiscoveryStatus.getNewUris().size(),
                    resourceDiscoveryStatus.getResourceCoordinates().getUri().toString());
        } catch (final IOException e) {
            log.error("{} [URI: {}]", e.getMessage(), resourceDiscoveryStatus.getResourceCoordinates().getUri());
            retry(resourceDiscoveryStatus.getResourceCoordinates());
        }
    }

    @Override
    protected boolean isSubmittable(final ResourceCoordinates resourceCoordinates) {
        return true;
    }

    protected String normalizeUri(final String uri) {

        String normalizedUri = uri;
        for (final LinkNormalizer normalizer : linkNormalizers) {
            normalizedUri = normalizer.normalize(normalizedUri);
        }
        return normalizedUri;
    }
}