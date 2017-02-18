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
package com.dattack.aranea.crawler.web;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author carlos
 *
 */
public class Repository {

    private static final Logger log = LoggerFactory.getLogger(Repository.class);
    
    private static final String CRAWLER_PATTERN_NAME = "crawler_%d.log";

    private File path;
    private volatile CrawlerLog crawlerLog;

    public Repository(final String path) {
        this.path = new File(path);
    }
    
    private CrawlerLog getCrawlerLog() throws FileNotFoundException {
        
        if (crawlerLog == null) {
            synchronized (this) {
                if (crawlerLog == null) {
                    final String crawlerFilename = String.format(CRAWLER_PATTERN_NAME, System.currentTimeMillis());
                    this.crawlerLog = new CrawlerLog(new FileOutputStream(new File(path, crawlerFilename)));
                }
            }
        }
        return crawlerLog;
    }

    public void write(final String filename, final String data, final PageInfo pageInfo) throws IOException {
        
        FileUtils.writeStringToFile(new File(path, filename), data);
        log.debug("{} stored as {}", pageInfo.getPage().getUri().toString(), filename);
        
        getCrawlerLog().write(pageInfo);
    }
}
