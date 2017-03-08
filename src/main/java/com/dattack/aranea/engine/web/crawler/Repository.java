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
package com.dattack.aranea.engine.web.crawler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dattack.aranea.beans.web.crawler.StorageBean;
import com.dattack.aranea.engine.Context;
import com.dattack.aranea.engine.ResourceDiscoveryStatus;

/**
 * @author cvarela
 * @since 0.1
 */
public class Repository {

    private static final Logger log = LoggerFactory.getLogger(Repository.class);

    private static final String CRAWLER_PATTERN_NAME = "crawler_%d.log";
    private static final DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");

    private final File path;
    private final FilenameGenerator filenameGenerator;
    private volatile OutputStream crawlerLog;

    public Repository(final StorageBean storageBean, final Context context) {
        this.path = new File(context.interpolate(storageBean.getRepository()));
        this.filenameGenerator = new FilenameGenerator(storageBean, context);
    }

    private OutputStream getCrawlerLog() throws IOException {

        if (crawlerLog == null) {
            synchronized (this) {
                if (crawlerLog == null) {
                    FileUtils.forceMkdir(path);
                    final String crawlerFilename = String.format(CRAWLER_PATTERN_NAME, System.currentTimeMillis());
                    this.crawlerLog = new FileOutputStream(new File(path, crawlerFilename));
                }
            }
        }
        return crawlerLog;
    }

    public void write(final ResourceDiscoveryStatus resourceDiscoveryStatus, final String data) throws IOException {

        final String filename = filenameGenerator
                .computeFilename(resourceDiscoveryStatus.getResourceCoordinates().getUri());

        if (filename != null) {
            FileUtils.writeStringToFile(new File(path, filename), data);
            log.debug("{} stored as {}", resourceDiscoveryStatus.getResourceCoordinates().getUri().toString(),
                    filename);
        }

        writeCrawlerLog(resourceDiscoveryStatus, filename);
    }

    private void writeCrawlerLog(final ResourceDiscoveryStatus status, final String filename) throws IOException {

        final StringBuilder builder = new StringBuilder() //
                .append(df.format(new Date())) //
                .append("\t").append(status.getNewUris().size()) //
                .append("\t").append(status.getAlreadyVisited().size()) //
                .append("\t").append(status.getIgnoredLinks().size() + status.getIgnoredUris().size()) //
                .append("\t").append(ObjectUtils.toString(status.getResourceCoordinates().getReferer())) //
                .append("\t").append(ObjectUtils.toString(status.getResourceCoordinates().getUri())) //
                .append("\t").append(StringUtils.trimToEmpty(filename)) //
                .append("\n");

        IOUtils.write(builder.toString(), getCrawlerLog());
    }
}
