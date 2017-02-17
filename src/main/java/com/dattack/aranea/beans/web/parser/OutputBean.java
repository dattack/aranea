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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;

import com.dattack.aranea.beans.XmlTokens;

/**
 * @author cvarela
 * @since 0.1
 */
public class OutputBean {

    @XmlAttribute(name = XmlTokens.DATA_FILE, required = true)
    private String datafile;

    @XmlElements({ @XmlElement(name = XmlTokens.REF, type = OutputItemRefBean.class),
            @XmlElement(name = XmlTokens.REPLACE, type = OutputItemReplaceBean.class),
            @XmlElement(name = XmlTokens.GROUP, type = OutputItemGroupBean.class) })
    private final List<OutputItemBean> columns;

    public OutputBean() {
        columns = new ArrayList<OutputItemBean>();
    }

    public String getDatafile() {
        return datafile;
    }

    public List<OutputItemBean> getColumns() {
        return columns;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("OutputBean [datafile=").append(datafile).append(", columns=").append(columns).append("]");
        return builder.toString();
    }
}
