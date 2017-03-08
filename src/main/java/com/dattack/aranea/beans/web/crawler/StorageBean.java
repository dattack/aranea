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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

import com.dattack.aranea.beans.XmlTokens;

/**
 * @author cvarela
 * @since 0.1
 */
public class StorageBean {

    @XmlEnum
    public enum Layout {
        @XmlEnumValue("plain") PLAIN,
        @XmlEnumValue("tree") TREE
    }

    @XmlAttribute(name = XmlTokens.FILENAME_PATTERN, required = true)
    private String filenamePattern;

    @XmlAttribute(name = XmlTokens.URL_REGEX)
    private String urlRegEx;

    @XmlAttribute(name = XmlTokens.LAYOUT)
    private final Layout layout;

    @XmlAttribute(name = XmlTokens.NOT_MATCHED_FILENAME_PATTERN)
    private String notMatchedFilenamePattern;
    
    @XmlAttribute(name = XmlTokens.REPOSITORY, required = false)
    private String repository;

    public StorageBean() {
        this.layout = Layout.TREE;
        this.repository = ".";
    }

    public String getNotMatchedFilenamePattern() {
        return notMatchedFilenamePattern;
    }

    public String getFilenamePattern() {
        return filenamePattern;
    }

    public Layout getLayout() {
        return layout;
    }

    public String getRepository() {
        return repository;
    }

    public String getUrlRegEx() {
        return urlRegEx;
    }
}
