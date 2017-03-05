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
package com.dattack.aranea.engine.web;

import java.util.List;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.PropertyConverter;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.dattack.aranea.beans.web.VarBean;
import com.dattack.aranea.util.JsoupUtil;

/**
 * @author cvarela
 * @since 0.1
 */
public class WebTaskUtil {
    
    private WebTaskUtil() {
        // utility class
    }

    public static void populateVars(final Element elem, final List<VarBean> varBeanList,
            final BaseConfiguration configuration) {

        for (VarBean varBean : varBeanList) {

            Elements varElements = elem
                    .select(PropertyConverter.interpolate(varBean.getSelector(), configuration).toString());

            if (varElements != null && !varElements.isEmpty()) {
                configuration.setProperty(varBean.getName(),
                        JsoupUtil.getElementValue(varElements.get(0), varBean.getValue()));
            }
        }
    }
}
