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
import java.util.List;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.PropertyConverter;
import org.apache.commons.lang.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dattack.aranea.beans.web.crawler.ExcludeBean;
import com.dattack.aranea.beans.web.crawler.RegionSelectorBean;
import com.dattack.aranea.beans.web.crawler.RequiredBean;
import com.dattack.aranea.beans.web.crawler.SeedBean;
import com.dattack.aranea.engine.ResourceCoordinates;
import com.dattack.aranea.engine.ResourceDiscoveryStatus;
import com.dattack.aranea.engine.web.WebTaskUtil;
import com.dattack.aranea.util.ThreadUtil;
import com.dattack.aranea.util.http.HttpResourceHelper;
import com.dattack.aranea.util.http.HttpResourceRequest;
import com.dattack.aranea.util.http.HttpResourceRequest.HttpResourceRequestBuilder;
import com.dattack.aranea.util.http.HttpResourceResponse;

/**
 * @author cvarela
 * @since 0.1
 */
public class CrawlerWebTask implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(CrawlerWebTask.class);

    private final ResourceCoordinates resourceCoordinates;
    private final CrawlerWebTaskController controller;

    public CrawlerWebTask(final ResourceCoordinates resourceCoordinates, final CrawlerWebTaskController controller) {
        this.resourceCoordinates = resourceCoordinates;
        this.controller = controller;
    }

    private boolean exclude(final String uri, final List<ExcludeBean> excludeBeanList) {

        for (final ExcludeBean bean : excludeBeanList) {
            if (uri.matches(controller.getContext().interpolate(bean.getRegex()))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void run() {

        ThreadUtil.sleep(controller.getSourceBean().getCrawler().getLatency());

        log.info("GET {}", resourceCoordinates.getUri());

        try {

            final HttpResourceRequest request = new HttpResourceRequestBuilder(resourceCoordinates).build();
            final HttpResourceResponse resource = HttpResourceHelper.get(request);
            final Document document = Parser.parse(resource.getData(), resourceCoordinates.getUri().toString());

            final ResourceDiscoveryStatus resourceDiscoveryStatus = submitUrisFromDocument(document);

            controller.handle(resourceDiscoveryStatus, document);

        } catch (final IOException e) {

            log.warn("{}: {} (Referer: {})", e.getMessage(), resourceCoordinates.getUri(),
                    resourceCoordinates.getReferer());
            controller.unrecoverable(resourceCoordinates);
        }
    }

    private void seedUrlsFromDocument(final ResourceDiscoveryStatus resourceDiscoveryStatus,
            final BaseConfiguration configuration) {

        // generate new links
        for (final SeedBean seedBean : controller.getCrawlerBean().getSeedBeanList()) {

            boolean missingRequiredVariables = false;
            for (final RequiredBean requiredBean : seedBean.getRequiredList()) {
                if (!configuration.containsKey(requiredBean.getName())) {
                    missingRequiredVariables = true;
                    break;
                }
            }

            if (missingRequiredVariables) {
                continue;
            }

            String link = seedBean.getUrl();
            try {

                link = PropertyConverter.interpolate(seedBean.getUrl(), configuration).toString();

                log.info("Seeding URL: {}", link);
                final URI seedUri = resourceCoordinates.getUri().resolve(link);

                submitUri(resourceDiscoveryStatus, seedUri.toString());

            } catch (final Throwable e) {
                log.warn("Document URL: {}, Child URL: {}, ERROR: {}", resourceCoordinates.getUri(), link,
                        e.getMessage());
            }
        }
    }

    private void submitUri(final ResourceDiscoveryStatus resourceDiscoveryStatus, final String uriAsText)
            throws URISyntaxException {

        final URI normalizedURI = new URI(controller.normalizeUri(uriAsText));

        if (!resourceDiscoveryStatus.isDuplicatedLink(normalizedURI)) {
            if (controller.submit(new ResourceCoordinates(normalizedURI, resourceCoordinates.getUri()))) {
                resourceDiscoveryStatus.addNewUri(normalizedURI);
            } else {
                resourceDiscoveryStatus.addAlreadyVisited(normalizedURI);
            }
        }
    }

    private ResourceDiscoveryStatus submitUrisFromDocument(final Document doc) {

        final ResourceDiscoveryStatus resourceDiscoveryStatus = new ResourceDiscoveryStatus(resourceCoordinates);
        // eval all variables, if one is present
        final BaseConfiguration configuration = new BaseConfiguration();
        WebTaskUtil.populateVars(doc, controller.getCrawlerBean().getVarBeanList(), configuration);

        // retrieve all document links
        for (final RegionSelectorBean regionSelectorBean : controller.getCrawlerBean().getRegionSelectorList()) {

            if (regionSelectorBean.getSelector() != null) {
                // scan only this area
                final Elements areas = doc.select(regionSelectorBean.getSelector());
                for (final Element element : areas) {
                    submitUrisFromElement(resourceDiscoveryStatus, element, regionSelectorBean);
                }
            } else {
                // scan the full document
                submitUrisFromElement(resourceDiscoveryStatus, doc, regionSelectorBean);
            }
        }

        seedUrlsFromDocument(resourceDiscoveryStatus, configuration);

        return resourceDiscoveryStatus;
    }

    private void submitUrisFromElement(final ResourceDiscoveryStatus resourceDiscoveryStatus, final Element element,
            final RegionSelectorBean domSelectorBean) {

        final Elements links = element.select(domSelectorBean.getElement());

        for (final Element link : links) {

            String linkHref = null;
            try {

                linkHref = controller.normalizeUri(StringUtils.trimToEmpty(link.attr(domSelectorBean.getAttribute())));
                if (StringUtils.isBlank(linkHref) //
                        || resourceDiscoveryStatus.getIgnoredLinks().contains(linkHref) //
                        || exclude(linkHref, domSelectorBean.getExcludeLinksList())) {
                    continue;
                }

                final URI linkUri = resourceCoordinates.getUri().resolve(linkHref);
                final String uriAsText = linkUri.toString();

                if (uriAsText.matches(controller.getContext().interpolate(domSelectorBean.getFilter()))
                        && !exclude(uriAsText, domSelectorBean.getExcludeUrlList())) {
                    submitUri(resourceDiscoveryStatus, uriAsText);
                } else {
                    resourceDiscoveryStatus.addIgnoredUri(linkUri);
                }

            } catch (final Throwable e) {
                log.warn("Document URL: {}, Link: {}, ERROR: {}", resourceCoordinates.getUri(), linkHref,
                        e.getMessage());
                resourceDiscoveryStatus.addIgnoredLink(linkHref);
            }
        }
    }
}
