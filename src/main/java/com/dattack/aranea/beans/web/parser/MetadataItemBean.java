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

import com.dattack.aranea.beans.XmlTokens;
import com.dattack.aranea.beans.web.VarBean;

/**
 * @author cvarela
 * @since 0.1
 */
public class MetadataItemBean {

    @XmlAttribute(name = XmlTokens.MANDATORY, required = false)
    private boolean mandatory;

    @XmlAttribute(name = XmlTokens.NAME, required = true)
    private String name;

    @XmlAttribute(name = XmlTokens.SELECTOR, required = true)
    private String selector;

    @XmlAttribute(name = XmlTokens.VALUE, required = false)
    private String value;
    
    @XmlAttribute(name = XmlTokens.INDEX, required = false)
    private int index;
    
    @XmlElement(name = XmlTokens.ITEM, required = false)
    private final List<MetadataItemBean> childItemList;
    
    @XmlElement(name = XmlTokens.VAR, required = false)
    private final List<VarBean> varBeanList;

    public MetadataItemBean() {
        this.childItemList = new ArrayList<MetadataItemBean>();
        this.varBeanList = new ArrayList<VarBean>();
        this.index = -1;
    }

    public List<MetadataItemBean> getChilldItemList() {
        return childItemList;
    }
    
    public List<VarBean> getVarBeanList() {
        return varBeanList;
    }
    
    public boolean isMandatory() {
        return mandatory;
    }

    public String getName() {
        return name;
    }

    public String getSelector() {
        return selector;
    }

    public String getValue() {
        return value;
    }
    
    public int getIndex() {
        return index;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("MetadataItemBean [name=").append(name).append(", selector=").append(selector).append(", value=")
                .append(value).append("]");
        return builder.toString();
    }
}
