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
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dattack.aranea.beans.web.WebBean;
import com.dattack.aranea.beans.web.crawler.CrawlerBean;
import com.dattack.aranea.beans.web.crawler.URINormalizerBean;
import com.dattack.aranea.engine.Context;
import com.dattack.aranea.engine.CrawlerTaskStatus;
import com.dattack.aranea.engine.ResourceCoordinates;
import com.dattack.aranea.engine.ResourceDiscoveryStatus;
import com.dattack.aranea.util.NamedThreadFactory;

/**
 * @author cvarela
 * @since 0.1
 */
class CrawlerWebTaskController {

    private static final Logger log = LoggerFactory.getLogger(CrawlerWebTaskController.class);

    private static final int MAX_ERRORS = 3;

    private final WebBean sourceBean;
    private final ThreadPoolExecutor executor;
    private final FilenameGenerator filenameGenerator;
    private final List<LinkNormalizer> linkNormalizers;
    private final CrawlerTaskStatus taskStatus;
    private final Repository repository;
    private final Context context;

    public CrawlerWebTaskController(final WebBean sourceBean) {

        this.sourceBean = sourceBean;
        this.context = new Context();
        this.repository = new Repository(getContext().interpolate(sourceBean.getRepository()));
        this.taskStatus = new CrawlerTaskStatus(MAX_ERRORS);
        this.filenameGenerator = new FilenameGenerator(getCrawlerBean().getStorageBean(), getContext());
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

                ResourceCoordinates resourceCoordinates = new ResourceCoordinates(
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
            taskStatus.registerAsVisited(resourceDiscoveryStatus.getResourceCoordinates());

            final String filename = filenameGenerator
                    .getFilename(resourceDiscoveryStatus.getResourceCoordinates().getUri());
            repository.write(filename, doc.html(), resourceDiscoveryStatus);

            tryShutdown();

            log.info("{} new URIs from {}", resourceDiscoveryStatus.getNewUris().size(),
                    resourceDiscoveryStatus.getResourceCoordinates().getUri().toString());
        } catch (final IOException e) {
            relaunch(resourceDiscoveryStatus.getResourceCoordinates());
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

    void fail(final ResourceCoordinates resourceCoordinates) {

        taskStatus.fail(resourceCoordinates);
        tryShutdown();
    }

    void relaunch(final ResourceCoordinates resourceCoordinates) {

        final boolean relaunch = taskStatus.relaunch(resourceCoordinates);
        if (relaunch) {
            submit(resourceCoordinates);
        } else {
            tryShutdown();
        }
    }

    protected boolean submit(final ResourceCoordinates resourceCoordinates) {

        try {
            if (taskStatus.submit(resourceCoordinates)) {
                executor.submit(new CrawlerWebTask(resourceCoordinates, this));
                return true;
            }
            return false;
        } catch (final Throwable t) {
            t.printStackTrace();
            return false;
        }
    }

}