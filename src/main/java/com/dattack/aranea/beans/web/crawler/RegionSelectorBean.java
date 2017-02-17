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
package com.dattack.aranea.beans.web.crawler;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import com.dattack.aranea.beans.XmlTokens;

/**
 * @author cvarela
 * @since 0.1
 */
public class RegionSelectorBean {

    @XmlAttribute(name = XmlTokens.SELECTOR, required = true)
    private String selector;

    @XmlAttribute(name = XmlTokens.ELEMENT, required = true)
    private String element;

    @XmlAttribute(name = XmlTokens.ATTRIBUTE, required = true)
    private String attribute;

    @XmlAttribute(name = XmlTokens.FILTER, required = false)
    private String filter;

    @XmlElement(name = XmlTokens.EXCLUDE_LINK, required = false)
    private final List<ExcludeBean> excludeLinksList;
    
    @XmlElement(name = XmlTokens.EXCLUDE_URL, required = false)
    private final List<ExcludeBean> excludeUrlsList;

    public RegionSelectorBean() {
        this.excludeLinksList = new ArrayList<ExcludeBean>();
        this.excludeUrlsList = new ArrayList<ExcludeBean>();
    }

    public String getElement() {
        return element;
    }

    public String getAttribute() {
        return attribute;
    }

    public String getFilter() {
        return filter;
    }

    public String getSelector() {
        return selector;
    }
    
    public List<ExcludeBean> getExcludeLinksList() {
        return excludeLinksList;
    }

    public List<ExcludeBean> getExcludeUrlList() {
        return excludeUrlsList;
    }
}
