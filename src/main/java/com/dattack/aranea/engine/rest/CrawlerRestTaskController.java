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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dattack.aranea.beans.appender.AbstractAppender;
import com.dattack.aranea.beans.appender.FileAppender;
import com.dattack.aranea.beans.jobs.Job;
import com.dattack.aranea.beans.rest.ResourceBean;
import com.dattack.aranea.beans.rest.RestBean;
import com.dattack.aranea.engine.CrawlerTaskController;
import com.dattack.aranea.engine.ResourceCoordinates;
import com.dattack.aranea.engine.ResourceDiscoveryStatus;
import com.dattack.aranea.engine.ResourceObject;
import com.dattack.aranea.util.JmxUtil;
import com.dattack.aranea.util.http.HttpResourceResponse;
import com.dattack.jtoolbox.commons.configuration.ConfigurationUtil;

/**
 * @author cvarela
 * @since 0.1
 */
public class CrawlerRestTaskController extends CrawlerTaskController {

    private static final Logger log = LoggerFactory.getLogger(CrawlerRestTaskController.class);

    private final RestBean restBean;
    private final Map<String, OutputStream> outputMapping;
    private final Set<Object> resourceIdList;

    public CrawlerRestTaskController(final RestBean restBean, final Job job) {

        super(3, restBean.getCrawlerBean().getThreadPoolSize(), restBean.getId(), job);
        this.restBean = restBean;
        this.outputMapping = new HashMap<>();
        this.resourceIdList = new HashSet<>();

        JmxUtil.registerMBean(this,
                JmxUtil.createObjectName(String.format("com.dattack.aranea.rest:type=%s,name=%s", //
                        this.getClass().getSimpleName(), //
                        restBean.getId())));
    }

    @Override
    protected Runnable createTask(final ResourceCoordinates resourceCoordinates) {

        final ResourceBean resourceBean = resourceLookup(resourceCoordinates.getUri().toString());
        if (resourceBean != null) {
            return new CrawlerRestTask(resourceCoordinates, this, resourceBean);
        }
        return null;
    }

    public void execute() {

        for (final String url : restBean.getCrawlerBean().getEntryPointList()) {
            try {
                final ResourceCoordinates resourceCoordinates = new ResourceCoordinates(
                        new URI(getContext().interpolate(url)));
                submit(resourceCoordinates);
            } catch (final URISyntaxException e) {
                log.error("Unable to go to the entry point: {}", e.getMessage());
            }
        }
    }

    private synchronized OutputStream getOutputStream(final String path) throws IOException {

        OutputStream outputStream = outputMapping.get(path);
        if (outputStream == null) {
            FileUtils.forceMkdir(new File(FilenameUtils.getPath(path)));
            outputStream = new FileOutputStream(new File(path));
            outputMapping.put(path, outputStream);
        }
        return outputStream;
    }

    protected void handle(final HttpResourceResponse response, final ResourceDiscoveryStatus resourceDiscoveryStatus,
            final List<ResourceObject> resources) {

        try {
            visited(response.getRequest().getResourceCoordinates());

            if (resources != null) {
                final ResourceBean resourceBean = resourceLookup(
                        response.getRequest().getResourceCoordinates().getUri().toString());
                if (resourceBean != null && resourceBean.hasAppenders()) {
                    handleResources(resourceBean, resources);
                }
            }

        } catch (final Exception e) {
            log.error("Unable to handle data from {} [Cause: {}]", response.getResourceUri(), e.getMessage());
            retry(response.getRequest().getResourceCoordinates());
        }
    }

    private void handleResources(final ResourceBean resourceBean, final List<ResourceObject> resourceList) {

        for (final ResourceObject resource : resourceList) {

            if (isUnique(resource)) {
                notifyAppenders(resource.compileConfiguration(), resourceBean.getAppenders().getAppenderList());
            }
        }
    }

    @Override
    protected boolean isSubmittable(final ResourceCoordinates resourceCoordinates) {

        final ResourceBean resourceBean = resourceLookup(resourceCoordinates.getUri().toString());
        return resourceBean != null;
    }

    private synchronized boolean isUnique(final ResourceObject resource) {

        final Object id = resource.getId();
        if (id != null) {
            if (resourceIdList.contains(id)) {
                return false;
            }

            resourceIdList.add(id);
        }
        return true;
    }

    private void notifyAppenders(final AbstractConfiguration configuration, final List<AbstractAppender> appenderList) {

        for (final AbstractAppender appender : appenderList) {

            if (appender instanceof FileAppender) {
                final FileAppender fileAppender = (FileAppender) appender;
                try {
                    final CompositeConfiguration compositeConfiguration = new CompositeConfiguration();
                    compositeConfiguration.setDelimiterParsingDisabled(false);
                    compositeConfiguration.addConfiguration(configuration);
                    compositeConfiguration.addConfiguration(getContext().getConfiguration());

                    final StringBuilder sb = new StringBuilder()
                            .append(ConfigurationUtil.interpolate(fileAppender.getPattern(), compositeConfiguration))
                            .append("\n");
                    IOUtils.write(sb.toString(), getOutputStream(getContext().interpolate(fileAppender.getFilename())));

                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private ResourceBean resourceLookup(final String uri) {

        for (final ResourceBean item : restBean.getResourceBeanList()) {

            final Pattern pattern = Pattern.compile(getContext().interpolate(item.getRegex()),
                    Pattern.CASE_INSENSITIVE);
            final Matcher matcher = pattern.matcher(uri);
            if (matcher.matches()) {
                return item;
            }
        }

        return null;
    }
}
