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
package com.dattack.aranea.beans.web;

import javax.xml.bind.annotation.XmlElement;

import com.dattack.aranea.beans.AbstractTaskBean;
import com.dattack.aranea.beans.XmlTokens;
import com.dattack.aranea.beans.web.crawler.CrawlerBean;
import com.dattack.aranea.beans.web.parser.ParserBean;

/**
 * @author cvarela
 * @since 0.1
 */
public class WebBean extends AbstractTaskBean {


    @XmlElement(name = XmlTokens.CRAWLER, required = true)
    private CrawlerBean crawler;

    @XmlElement(name = XmlTokens.PARSER, required = true)
    private ParserBean parser;

    public CrawlerBean getCrawler() {
        return crawler;
    }

    public ParserBean getParser() {
        return parser;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SourceBean [id=").append(getId()).append(", crawler=").append(crawler).append(", parser=")
        .append(parser).append("]");
        return builder.toString();
    }
}
