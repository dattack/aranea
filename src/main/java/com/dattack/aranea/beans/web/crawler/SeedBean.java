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
package com.dattack.aranea.beans.web.crawler;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import com.dattack.aranea.beans.XmlTokens;

/**
 * @author cvarela
 * @since 0.1
 */
public class SeedBean {

    @XmlAttribute(name = XmlTokens.URL, required = true)
    private String url;

    @XmlElement(name = XmlTokens.REQUIRED, required = true)
    private List<RequiredBean> requiredList;

    public List<RequiredBean> getRequiredList() {
        return requiredList;
    }

    public String getUrl() {
        return url;
    }
}
