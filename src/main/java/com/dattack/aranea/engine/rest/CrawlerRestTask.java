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

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dattack.aranea.beans.HeaderBean;
import com.dattack.aranea.beans.rest.ResourceBean;
import com.dattack.aranea.engine.ResourceCoordinates;
import com.dattack.aranea.engine.ResourceDiscoveryStatus;
import com.dattack.aranea.engine.ResourceObject;
import com.dattack.aranea.util.ThreadUtil;
import com.dattack.aranea.util.http.HttpResourceHelper;
import com.dattack.aranea.util.http.HttpResourceRequest;
import com.dattack.aranea.util.http.HttpResourceRequest.HttpResourceRequestBuilder;
import com.dattack.aranea.util.http.HttpResourceResponse;
import com.dattack.jtoolbox.commons.configuration.ConfigurationUtil;
import com.dattack.jtoolbox.script.JavaScriptEngine;

/**
 * @author cvarela
 * @since 0.1
 */
class CrawlerRestTask implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(CrawlerRestTask.class);

    private static final String RESOURCE_URI = "resource_uri";
    private static final String RESOURCE_REFERER = "resource_referer";

    private static final String LINKS_KEY = "links";
    private static final String DATA_KEY = "data";
    private static final String METHOD_KEY = "method";
    private static final String URI_KEY = "uri";

    private final ResourceCoordinates resourceCoordinates;
    private final CrawlerRestTaskController controller;
    private final ResourceBean resourceBean;
    private final AbstractConfiguration configuration;

    public CrawlerRestTask(final ResourceCoordinates resourceCoordinates, final CrawlerRestTaskController controller,
            final ResourceBean resourceBean) {
        this.resourceCoordinates = resourceCoordinates;
        this.controller = controller;
        this.resourceBean = resourceBean;
        // this.configuration = controller.getContext().getConfiguration();
        this.configuration = new BaseConfiguration();
        populateConfiguration();
    }

    private HttpResourceRequest createRequest() {

        final HttpResourceRequestBuilder builder = new HttpResourceRequestBuilder(resourceCoordinates);

        for (final HeaderBean headerBean : resourceBean.getHeaders().getHeaderList()) {
            builder.withHeader(headerBean.getName(), headerBean.getValue());
        }
        return builder.build();
    }

    private Object evalJavascript(final String json) throws ScriptException {

        final Map<Object, Object> params = new HashMap<>();
        params.put("data", json);

        return JavaScriptEngine.eval(resourceBean.getScript(), params);
    }

    private List<ResourceObject> extractData(final Object obj) {

        List<ResourceObject> resourceList = null;
        if (obj != null && obj instanceof Bindings) {

            final Bindings bindings = (Bindings) obj;
            if (!bindings.isEmpty()) {
                resourceList = new ArrayList<>();
                for (final Object objItem : bindings.values()) {
                    final ResourceObject resourceObject = extractDataItem(objItem);
                    if (resourceObject != null) {
                        resourceList.add(resourceObject);
                    }
                }
            }
        }
        return resourceList;
    }

    private ResourceObject extractDataItem(final Object obj) {

        if (obj != null && obj instanceof Bindings) {
            final Bindings bindings = (Bindings) obj;
            if (!bindings.isEmpty()) {
                return new ResourceObject(bindings);
            }
        }

        return null;
    }

    private void populateConfiguration() {

        this.configuration.setProperty(RESOURCE_URI, resourceCoordinates.getUri().toString());
        if (resourceCoordinates.getReferer() == null) {
            this.configuration.setProperty(RESOURCE_REFERER, "");
        } else {
            this.configuration.setProperty(RESOURCE_REFERER, resourceCoordinates.getReferer().toString());
        }
    }

    @Override
    public void run() {

        // TODO: set a proper latency time
        ThreadUtil.sleep(1000);

        log.info("GET {}", resourceCoordinates.getUri());

        try {

            if (StringUtils.isNotBlank(resourceBean.getScript())) {

                final HttpResourceRequest request = createRequest();
                final HttpResourceResponse response = HttpResourceHelper.get(request);

                if (response.hasData()) {

                    if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                        final Object jsResult = evalJavascript(response.getData());

                        List<ResourceObject> resourceList = null;
                        ResourceDiscoveryStatus resourceDiscoveryStatus = null;
                        if (jsResult instanceof Bindings) {
                            final Bindings bindings = (Bindings) jsResult;
                            resourceList = extractData(bindings.get(DATA_KEY));
                            resourceDiscoveryStatus = submitLinks(bindings.get(LINKS_KEY));
                        }
                        controller.handle(response, resourceDiscoveryStatus, resourceList);
                    } else if (response.getStatusLine().getStatusCode() >= HttpStatus.SC_BAD_REQUEST) {
                        controller.fail(resourceCoordinates);
                    } else {
                        // TODO: is 'relaunch' the right action?
                        controller.relaunch(resourceCoordinates);
                    }

                } else {
                    log.error("Unable to process an empty resource (no text data retrieved): {}",
                            resourceCoordinates.toString());
                    controller.fail(resourceCoordinates);
                }
            } else {
                log.error("Unable to process a resource without javascript configuration: {}",
                        resourceCoordinates.toString());
                controller.fail(resourceCoordinates);
            }

        } catch (final IOException e) {

            log.warn("{}: {}", e.getMessage(), resourceCoordinates.toString());
            controller.relaunch(resourceCoordinates);

        } catch (final ScriptException e) {

            log.error("{}: {}", e.getMessage(), resourceCoordinates.toString());
            controller.fail(resourceCoordinates);
        }
    }

    private void submitLinkItem(final ResourceDiscoveryStatus resourceDiscoveryStatus, final Object obj) {

        if (obj != null && obj instanceof Bindings) {
            final Bindings bindings = (Bindings) obj;

            // TODO: process headers
            // processHeader(item.get(HEADER_KEY));

            ObjectUtils.toString(bindings.get(METHOD_KEY));
            final String link = ConfigurationUtil.interpolate(ObjectUtils.toString(bindings.get(URI_KEY)),
                    configuration);
            try {

                final ResourceCoordinates linkCoordinates = new ResourceCoordinates(new URI(link),
                        resourceCoordinates.getUri());
                if (controller.submit(linkCoordinates)) {
                    resourceDiscoveryStatus.addNewUri(linkCoordinates.getUri());
                } else {
                    resourceDiscoveryStatus.addAlreadyVisited(linkCoordinates.getUri());
                }
            } catch (final URISyntaxException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private ResourceDiscoveryStatus submitLinks(final Object obj) {

        final ResourceDiscoveryStatus resourceDiscoveryStatus = new ResourceDiscoveryStatus(resourceCoordinates);
        if (obj != null && obj instanceof Bindings) {
            final Bindings bindings = (Bindings) obj;
            for (final Entry<String, Object> entry : bindings.entrySet()) {
                submitLinkItem(resourceDiscoveryStatus, entry.getValue());
            }
        }
        return resourceDiscoveryStatus;
    }
}
