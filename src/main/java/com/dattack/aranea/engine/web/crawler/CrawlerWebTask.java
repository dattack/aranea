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
import org.jsoup.HttpStatusException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dattack.aranea.beans.web.crawler.ExcludeBean;
import com.dattack.aranea.beans.web.crawler.RegionSelectorBean;
import com.dattack.aranea.beans.web.crawler.SeedBean;
import com.dattack.aranea.engine.Context;
import com.dattack.aranea.engine.Page;
import com.dattack.aranea.engine.PageInfo;
import com.dattack.aranea.engine.ResourceCoordinates;
import com.dattack.aranea.util.ThreadUtil;
import com.dattack.aranea.util.WebTaskUtil;
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
            
            PageInfo pageInfo = new PageInfo(page, "OK");
            submitUrisFromDocument(document, pageInfo);

            controller.handle(pageInfo, document);

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
    

    private static boolean exclude(final String uri, final List<ExcludeBean> excludeBeanList) {

        for (final ExcludeBean bean : excludeBeanList) {
            if (uri.matches(Context.get().interpolate(bean.getRegex()))) {
                return true;
            }
        }
        return false;
    }
    
    private void submitUrisFromDocument(final Document doc, final PageInfo pageInfo) {

        // eval all variables, if one is present
        BaseConfiguration configuration = new BaseConfiguration();
        WebTaskUtil.populateVars(doc, controller.getCrawlerBean().getVarBeanList(), configuration);

        // retrieve all document links
        for (final RegionSelectorBean regionSelectorBean : controller.getCrawlerBean().getRegionSelectorList()) {

            if (regionSelectorBean.getSelector() != null) {
                // scan only this area
                final Elements areas = doc.select(regionSelectorBean.getSelector());
                for (final Element element : areas) {
                    submitUrisFromElement(element, regionSelectorBean, pageInfo);
                }
            } else {
                // scan the full document
                submitUrisFromElement(doc, regionSelectorBean, pageInfo);
            }
        }

        seedUrlsFromDocument(configuration, pageInfo);
    }
    

    private void seedUrlsFromDocument(final BaseConfiguration configuration, final PageInfo pageInfo) {

        // generate new links
        for (SeedBean seedBean : controller.getCrawlerBean().getSeedBeanList()) {

            String link = seedBean.getUrl();
            try {

                link = PropertyConverter.interpolate(seedBean.getUrl(), configuration).toString();

                log.info("Seeding URL: {}", link);
                final URI seedUri = pageInfo.getPage().getUri().resolve(link);

                submitUri(pageInfo, seedUri.toString(), pageInfo.getPage().getUri());

            } catch (final Throwable e) {
                log.warn("Document URL: {}, Child URL: {}, ERROR: {}", pageInfo.getPage().getUri(), link,
                        e.getMessage());
            }
        }
    }

    private void submitUrisFromElement(final Element element, final RegionSelectorBean domSelectorBean,
            final PageInfo pageInfo) {

        final Elements links = element.select(domSelectorBean.getElement());

        for (final Element link : links) {

            String linkHref = null;
            try {

                linkHref = StringUtils.trimToEmpty(link.attr(domSelectorBean.getAttribute()));
                if (StringUtils.isBlank(linkHref) || pageInfo.getIgnoredLinks().contains(linkHref)
                        || exclude(linkHref, domSelectorBean.getExcludeLinksList())) {
                    continue;
                }

                final URI linkUri = pageInfo.getPage().getUri().resolve(linkHref);
                final String uriAsText = linkUri.toString();

                if (uriAsText.matches(Context.get().interpolate(domSelectorBean.getFilter()))
                        && !exclude(uriAsText, domSelectorBean.getExcludeUrlList())) {
                    submitUri(pageInfo, uriAsText, pageInfo.getPage().getUri());
                } else {
                    pageInfo.addIgnoredUri(linkUri);
                }

            } catch (final Throwable e) {
                log.warn("Document URL: {}, Link: {}, ERROR: {}", pageInfo.getPage().getUri(), linkHref,
                        e.getMessage());
                pageInfo.addIgnoredLink(linkHref);
            }
        }
    }
    
    private void submitUri(final PageInfo pageInfo, final String uriAsText, final URI referer)
            throws URISyntaxException {

        final URI normalizedURI = new URI(controller.normalizeUri(uriAsText));

        if (!pageInfo.isDuplicatedLink(normalizedURI)) {
            if (controller.submit(new Page(normalizedURI, referer))) {
                pageInfo.addNewUri(normalizedURI);
            } else {
                pageInfo.addVisitedUri(normalizedURI);
            }
        }
    }
}
