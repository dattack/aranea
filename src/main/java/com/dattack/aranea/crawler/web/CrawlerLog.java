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

import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ObjectUtils;

/**
 * @author cvarela
 * @since 0.1
 */
public class CrawlerLog {

    private final OutputStream outputStream;

    public CrawlerLog(final OutputStream fileOutputStream) {
        this.outputStream = fileOutputStream;
    }

    public void write(final PageInfo pageInfo) throws IOException {

        StringBuilder builder = new StringBuilder() //
                .append(ObjectUtils.toString(pageInfo.getPage().getReferer())) //
                .append("\t") //
                .append(ObjectUtils.toString(pageInfo.getPage().getUri())) //
                .append("\n");

        IOUtils.write(builder.toString(), outputStream);
    }
}
