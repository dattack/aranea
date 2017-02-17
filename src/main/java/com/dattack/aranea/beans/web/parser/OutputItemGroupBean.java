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
package com.dattack.aranea.beans.web.parser;

import javax.xml.bind.annotation.XmlAttribute;

import com.dattack.aranea.beans.XmlTokens;

/**
 * @author cvarela
 * @since 0.1
 */
public class OutputItemGroupBean implements OutputItemBean {

    @XmlAttribute(name = XmlTokens.NAME, required = true)
    private String name;

    @XmlAttribute(name = XmlTokens.REF, required = true)
    private String ref;

    @XmlAttribute(name = XmlTokens.REGEX, required = true)
    private String regex;

    @XmlAttribute(name = XmlTokens.GROUP, required = true)
    private final int group;

    public OutputItemGroupBean() {
        this.group = 0;
    }

    public String getName() {
        return name;
    }

    public String getRef() {
        return ref;
    }

    public String getRegex() {
        return regex;
    }

    public int getGroup() {
        return group;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("OutputColumnReplaceBean[ref=").append(ref).append(", regex=").append(regex).append(", group=")
        .append(group).append("]");
        return builder.toString();
    }

    @Override
    public void accept(final OutputItemVisitor visitor) {
        visitor.visite(this);
    }
}
