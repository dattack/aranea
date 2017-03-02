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

import org.jsoup.HttpStatusException;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dattack.aranea.engine.Page;
import com.dattack.aranea.engine.PageInfo;
import com.dattack.aranea.engine.ResourceCoordinates;
import com.dattack.aranea.util.ThreadUtil;
import com.dattack.aranea.util.http.HttpResourceHelper;
import com.dattack.aranea.util.http.HttpResourceRequest;
import com.dattack.aranea.util.http.HttpResourceRequest.HttpResourceRequestBuilder;
import com.dattack.aranea.util.http.HttpResourceResponse;

/**
 * @author cvarela
 * @since 0.1
 */
class CrawlerWebTask implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(CrawlerWebTask.class);

    private final Page page;
    private final CrawlerWebTaskController controller;

    public CrawlerWebTask(final Page page, final CrawlerWebTaskController controller) {
        this.page = page;
        this.controller = controller;
    }

    @Override
    public void run() {

        ThreadUtil.sleep(controller.getSourceBean().getCrawler().getLatency());

        log.info("GET {}", page.getUri());

        try {

            ResourceCoordinates resourceCoordinates = new ResourceCoordinates(page.getUri(), page.getReferer());
            HttpResourceRequest request = new HttpResourceRequestBuilder(resourceCoordinates).build();
            HttpResourceResponse resource = HttpResourceHelper.get(request);
            Document document = Parser.parse(resource.getData(), page.getUri().toString());

            controller.handle(new PageInfo(page, "OK"), document);

        } catch (final HttpStatusException e) {

            log.warn("[{}] {}: {} (Referer: {})", e.getStatusCode(), e.getMessage(), page.getUri(), page.getReferer());
            PageInfo pageInfo = new PageInfo(page, e.getStatusCode(), e.getMessage());
            controller.relaunch(pageInfo);

        } catch (IOException e) {

            log.warn("{}: {} (Referer: {})", e.getMessage(), page.getUri(), page.getReferer());
            PageInfo pageInfo = new PageInfo(page, e.getMessage());
            controller.fail(pageInfo);
        }
    }
}
