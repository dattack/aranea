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
package com.dattack.aranea.engine.rest;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.script.Bindings;
import javax.script.ScriptException;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.jsoup.HttpStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dattack.aranea.beans.rest.ResourceBean;
import com.dattack.aranea.engine.Context;
import com.dattack.aranea.engine.Page;
import com.dattack.aranea.engine.PageInfo;
import com.dattack.jtoolbox.script.JavaScriptEngine;

/**
 * @author cvarela
 * @since 0.1
 */
class CrawlerRestTask implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(CrawlerRestTask.class);

    private static final String LINKS_KEY = "links";
    private static final String DATA_KEY = "data";
    private static final String METHOD_KEY = "method";
    private static final String URI_KEY = "uri";

    private final Page page;
    private final CrawlerRestTaskController controller;
    private final ResourceBean resourceBean;

    public CrawlerRestTask(final Page page, final CrawlerRestTaskController controller,
            final ResourceBean resourceBean) {
        this.page = page;
        this.controller = controller;
        this.resourceBean = resourceBean;
    }

    private List<Map<String, Object>> processData(final Object obj) {

        List<Map<String, Object>> resourceList = null;
        if (obj != null && obj instanceof Bindings) {

            final Bindings bindings = (Bindings) obj;
            if (!bindings.isEmpty()) {
                resourceList = new ArrayList<>();
                for (final Object objItem : bindings.values()) {
                    final Map<String, Object> map = processDataItem(objItem);
                    if (map != null) {
                        resourceList.add(map);
                    }
                }
            }
        }
        return resourceList;
    }

    private Map<String, Object> processDataItem(final Object obj) {

        if (obj != null && obj instanceof Bindings) {
            final Bindings bindings = (Bindings) obj;
            if (!bindings.isEmpty()) {
                return bindings;
            }
        }

        return null;
    }

    private void processLinkItem(final Object obj) {

        if (obj != null && obj instanceof Bindings) {
            final Bindings bindings = (Bindings) obj;

            // TODO: process headers
            // processHeader(item.get(HEADER_KEY));

            ObjectUtils.toString(bindings.get(METHOD_KEY));
            final String link = ObjectUtils.toString(bindings.get(URI_KEY));
            try {
                controller.submit(new Page(new URI(Context.get().interpolate(link))));
            } catch (URISyntaxException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private void processLinks(final Object obj) {

        if (obj != null && obj instanceof Bindings) {
            final Bindings bindings = (Bindings) obj;
            for (final Entry<String, Object> entry : bindings.entrySet()) {
                processLinkItem(entry.getValue());
            }
        }
    }

    @Override
    public void run() {

        // ThreadUtil.sleep(controller.getSourceBean().getCrawler().getLatency());

        log.info("GET {}", page.getUri());

        try {

            List<Map<String, Object>> resourceList = null;
            if (StringUtils.isNotBlank(resourceBean.getScript())) {

                final String content = HttpCrawler.get(page.getUri());

                if (StringUtils.isNotBlank(content)) {
                    final Map<Object, Object> params = new HashMap<>();
                    params.put("data", content);
                    final Object jsResult = JavaScriptEngine.eval(resourceBean.getScript(), params);
                    if (jsResult instanceof Bindings) {
                        final Bindings bindings = (Bindings) jsResult;
                        resourceList = processData(bindings.get(DATA_KEY));
                        processLinks(bindings.get(LINKS_KEY));
                    }
                } else {
                    // TODO: the resource returns no data
                }
            } else {
                // TODO: unable to process resource without JS-script 
            }

            controller.handle(new PageInfo(page, "OK"), resourceList);

        } catch (final HttpStatusException e) {

            log.warn("[{}] {}: {} (Referer: {})", e.getStatusCode(), e.getMessage(), page.getUri(), page.getReferer());
            PageInfo pageInfo = new PageInfo(page, e.getStatusCode(), e.getMessage());
            controller.relaunch(pageInfo);

        } catch (IOException e) {

            log.warn("{}: {} (Referer: {})", e.getMessage(), page.getUri(), page.getReferer());
            PageInfo pageInfo = new PageInfo(page, e.getMessage());
            controller.fail(pageInfo);
        } catch (ScriptException e) {
            log.warn("{}: {} (Referer: {})", e.getMessage(), page.getUri(), page.getReferer());
            PageInfo pageInfo = new PageInfo(page, e.getMessage());
            controller.fail(pageInfo);
        }
    }
}
