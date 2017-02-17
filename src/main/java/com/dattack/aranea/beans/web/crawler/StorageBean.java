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

import com.dattack.aranea.beans.XmlTokens;

/**
 * @author cvarela
 * @since 0.1
 */
public class StorageBean {

    public static final String PLAIN_LAYAOUT = "plain";
    public static final String TREE_LAYOUT = "tree";

    @XmlAttribute(name = XmlTokens.FILENAME_PATTERN, required = true)
    private String filenamePattern;

    @XmlAttribute(name = XmlTokens.URL_REGEX)
    private String urlRegEx;

    @XmlAttribute(name = XmlTokens.LAYOUT)
    private final String layout;

    @XmlAttribute(name = XmlTokens.STORE_NOT_MATCHING)
    private final boolean storeNotMatching;

    @XmlAttribute(name = XmlTokens.FILENAME_NOT_MATCHING_PATTERN)
    private String filenameNotMatchingPattern;

    public StorageBean() {
        this.storeNotMatching = true;
        this.layout = PLAIN_LAYAOUT;
    }

    public String getFilenamePattern() {
        return filenamePattern;
    }

    public String getUrlRegEx() {
        return urlRegEx;
    }

    public boolean isStoreNotMatching() {
        return storeNotMatching;
    }

    public String getFilenameNotMatchingPattern() {
        return filenameNotMatchingPattern;
    }

    public String getLayout() {
        return layout;
    }
}
