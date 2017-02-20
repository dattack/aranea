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
package com.dattack.aranea.parser.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.PropertyConverter;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.dattack.aranea.beans.web.parser.MetadataBean;
import com.dattack.aranea.beans.web.parser.MetadataItemBean;
import com.dattack.aranea.util.JsoupUtil;
import com.dattack.aranea.util.WebTaskUtil;

/**
 * @author cvarela
 * @since 0.1
 */
final class DataExtractor {

    private final MetadataBean metadata;

    public DataExtractor(final MetadataBean metadata) {
        this.metadata = metadata;
    }

    public Map<String, Object> execute(final Document doc) throws MissingAttributeException {

        final Map<String, Object> map = new HashMap<String, Object>(metadata.getMetadataItemList().size());

        for (MetadataItemBean columnBean : metadata.getMetadataItemList()) {
            Object obj = lookup(doc, columnBean);
            if (columnBean.isMandatory() && (obj == null)) {
                throw new MissingAttributeException(columnBean.getName());
            }
            map.put(columnBean.getName(), obj);
        }

        return map;
    }

    private Object lookup(final Element doc, final MetadataItemBean columnBean) {
        return lookup(doc, columnBean, new BaseConfiguration());
    }

    private Object lookup(final Element doc, final MetadataItemBean columnBean, final BaseConfiguration configuration) {

        Elements elements = doc
                .select(PropertyConverter.interpolate(columnBean.getSelector(), configuration).toString());

        if (columnBean.getIndex() >= 0 && elements.size() > columnBean.getIndex()) {

            Element element = elements.get(columnBean.getIndex());
            elements = new Elements();
            elements.add(element);
        }

        if (elements != null) {

            if (elements.size() > 1 || !columnBean.getChilldItemList().isEmpty()) {
                final List<Object> values = new ArrayList<Object>();
                for (Element elem : elements) {
                    String elementValue = JsoupUtil.getElementValue(elem, columnBean.getValue());
                    if (!columnBean.getChilldItemList().isEmpty()) {

                        configuration.setProperty(columnBean.getName(), elementValue);
                        WebTaskUtil.populateVars(elem, columnBean.getVarBeanList(), configuration);

                        Map<String, Object> childMap = new HashMap<String, Object>();
                        for (MetadataItemBean childItem : columnBean.getChilldItemList()) {
                            childMap.put(childItem.getName(), lookup(doc, childItem, configuration));
                        }
                        Map<String, Object> map = new HashMap<String, Object>();
                        map.put(elementValue, childMap);
                        values.add(map);
                    } else {
                        values.add(elementValue);
                    }
                }
                return values;
            }

            String value = null;
            if (elements.size() == 1) {
                value = JsoupUtil.getElementValue(elements.get(0), columnBean.getValue());
            }
            return value;
        }

        // element not found
        return null;
    }
}
