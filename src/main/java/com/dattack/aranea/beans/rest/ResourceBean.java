/*
 * Copyright (c) 2016, The Dattack team (http://www.dattack.com)
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
package com.dattack.aranea.beans.rest;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import com.dattack.aranea.beans.XmlTokens;
import com.dattack.aranea.beans.appender.AppendersBean;
import com.dattack.jtoolbox.util.CollectionUtils;

/**
 * @author cvarela
 * @since 0.1
 */
public class ResourceBean {

    @XmlAttribute(name = XmlTokens.REGEX, required = true)
    private String regex;

    @XmlAttribute(name = XmlTokens.METHOD, required = true)
    private String method;

    @XmlElement(name = XmlTokens.SCRIPT, required = true)
    private String script;

    @XmlElement(name = "appenders", required = false, nillable = false)
    private AppendersBean appenders;

    public AppendersBean getAppenders() {
        return appenders;
    }

    public String getMethod() {
        return method;
    }

    public String getRegex() {
        return regex;
    }

    public String getScript() {
        return script;
    }
    
    public boolean hasAppenders() {
        return appenders != null && CollectionUtils.isNotEmpty(appenders.getAppenderList());
    }
}
