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

import com.dattack.aranea.beans.web.WebBean;
import com.dattack.aranea.beans.web.crawler.CrawlerBean;
import com.dattack.aranea.beans.web.crawler.URINormalizerBean;
import com.dattack.aranea.engine.Context;
import com.dattack.aranea.engine.CrawlerTaskController;
import com.dattack.aranea.engine.ResourceCoordinates;
import com.dattack.aranea.engine.ResourceDiscoveryStatus;

/**
 * @author cvarela
 * @since 0.1
 */
public class CrawlerWebTaskController extends CrawlerTaskController {

    private static final Logger log = LoggerFactory.getLogger(CrawlerWebTaskController.class);

    private static final int MAX_ERRORS = 3;

    private final WebBean sourceBean;
    private final FilenameGenerator filenameGenerator;
    private final List<LinkNormalizer> linkNormalizers;
    private final Repository repository;
    private final Context context;

    public CrawlerWebTaskController(final WebBean sourceBean) {
        super(MAX_ERRORS, sourceBean.getCrawler().getThreadPoolSize(), sourceBean.getId());
        this.sourceBean = sourceBean;
        this.context = new Context();
        this.repository = new Repository(getContext().interpolate(sourceBean.getRepository()));
        this.filenameGenerator = new FilenameGenerator(getCrawlerBean().getStorageBean(), getContext());
        this.linkNormalizers = new ArrayList<>();
        for (final URINormalizerBean lnc : getCrawlerBean().getNormalizerList()) {
            linkNormalizers.add(new LinkNormalizer(lnc));
        }
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

    public Context getContext() {
        return context;
    }

    protected CrawlerBean getCrawlerBean() {
        return getSourceBean().getCrawler();
    }

    WebBean getSourceBean() {
        return sourceBean;
    }

    int getTimeout() {
        return getCrawlerBean().getTimeout();
    }

    void handle(final ResourceDiscoveryStatus resourceDiscoveryStatus, final Document doc) {

        try {
            visited(resourceDiscoveryStatus.getResourceCoordinates());

            final String filename = filenameGenerator
                    .getFilename(resourceDiscoveryStatus.getResourceCoordinates().getUri());
            repository.write(filename, doc.html(), resourceDiscoveryStatus);

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