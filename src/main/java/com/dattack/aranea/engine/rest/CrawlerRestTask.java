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
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dattack.aranea.beans.HeaderBean;
import com.dattack.aranea.beans.rest.ResourceBean;
import com.dattack.aranea.engine.Context;
import com.dattack.aranea.engine.ResourceCoordinates;
import com.dattack.aranea.engine.ResourceDiscoveryStatus;
import com.dattack.aranea.util.http.HttpResourceHelper;
import com.dattack.aranea.util.http.HttpResourceRequest;
import com.dattack.aranea.util.http.HttpResourceRequest.HttpResourceRequestBuilder;
import com.dattack.aranea.util.http.HttpResourceResponse;
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

    private final ResourceCoordinates resourceCoordinates;
    private final CrawlerRestTaskController controller;
    private final ResourceBean resourceBean;

    public CrawlerRestTask(final ResourceCoordinates resourceCoordinates, final CrawlerRestTaskController controller,
            final ResourceBean resourceBean) {
        this.resourceCoordinates = resourceCoordinates;
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

    private void processLinkItem(final ResourceDiscoveryStatus resourceDiscoveryStatus, final Object obj) {

        if (obj != null && obj instanceof Bindings) {
            final Bindings bindings = (Bindings) obj;

            // TODO: process headers
            // processHeader(item.get(HEADER_KEY));

            ObjectUtils.toString(bindings.get(METHOD_KEY));
            final String link = ObjectUtils.toString(bindings.get(URI_KEY));
            try {

                ResourceCoordinates linkCoordinates = new ResourceCoordinates(new URI(Context.get().interpolate(link)),
                        resourceCoordinates.getUri());
                if (controller.submit(linkCoordinates)) {
                    resourceDiscoveryStatus.addNewUri(linkCoordinates.getUri());
                } else {
                    resourceDiscoveryStatus.addAlreadyVisited(linkCoordinates.getUri());
                }
            } catch (URISyntaxException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private ResourceDiscoveryStatus processLinks(final Object obj) {

        ResourceDiscoveryStatus resourceDiscoveryStatus = new ResourceDiscoveryStatus(resourceCoordinates);
        if (obj != null && obj instanceof Bindings) {
            final Bindings bindings = (Bindings) obj;
            for (final Entry<String, Object> entry : bindings.entrySet()) {
                processLinkItem(resourceDiscoveryStatus, entry.getValue());
            }
        }
        return resourceDiscoveryStatus;
    }

    private HttpResourceRequest createRequest() {

        HttpResourceRequestBuilder builder = new HttpResourceRequestBuilder(resourceCoordinates);

        for (final HeaderBean headerBean : resourceBean.getHeaders().getHeaderList()) {
            builder.withHeader(headerBean.getName(), headerBean.getValue());
        }
        return builder.build();
    }

    @Override
    public void run() {

        // ThreadUtil.sleep(controller.getSourceBean().getCrawler().getLatency());

        log.info("GET {}", resourceCoordinates.getUri());

        try {

            if (StringUtils.isNotBlank(resourceBean.getScript())) {

                final HttpResourceRequest request = createRequest();
                final HttpResourceResponse response = HttpResourceHelper.get(request);

                if (response.hasData()) {

                    final Map<Object, Object> params = new HashMap<>();
                    params.put("data", response.getData());

                    final Object jsResult = JavaScriptEngine.eval(resourceBean.getScript(), params);

                    List<Map<String, Object>> resourceList = null;
                    ResourceDiscoveryStatus resourceDiscoveryStatus = null;
                    if (jsResult instanceof Bindings) {
                        final Bindings bindings = (Bindings) jsResult;
                        resourceList = processData(bindings.get(DATA_KEY));
                        resourceDiscoveryStatus = processLinks(bindings.get(LINKS_KEY));
                    }

                    if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                        controller.handle(response, resourceList);
                    } else if (response.getStatusLine().getStatusCode() >= HttpStatus.SC_BAD_REQUEST) {
                        controller.fail(resourceCoordinates);
                    } else {
                        // TODO: is 'relaunch' the right action?
                        controller.relaunch(resourceCoordinates);
                    }

                } else {
                    // TODO: the resource returns no data
                }
            } else {
                // TODO: unable to process resource without JS-script
            }

            controller.fail(resourceCoordinates);

        } catch (IOException e) {

            log.warn("{}: {} (Referer: {})", e.getMessage(), resourceCoordinates.getUri(),
                    resourceCoordinates.getReferer());
            controller.relaunch(resourceCoordinates);

        } catch (ScriptException e) {

            log.warn("{}: {} (Referer: {})", e.getMessage(), resourceCoordinates.getUri(),
                    resourceCoordinates.getReferer());
            controller.fail(resourceCoordinates);
        }
    }
}
