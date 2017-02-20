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
package com.dattack.aranea.util;

import org.jsoup.nodes.Element;

/**
 * @author cvarela
 * @since 0.1
 */
public final class JsoupUtil {
    
    private JsoupUtil() {
        // utility class
    }

    public static String getElementValue(final Element element, final String expression) {

        if (expression != null) {
            if (expression.startsWith("attr:")) {
                String attributeKey = "src";
                String[] tokens = expression.split(":");
                if (tokens.length > 1) {
                    attributeKey = tokens[1];
                }
                return element.attr(attributeKey);
            } else if (expression.equalsIgnoreCase("html")) {
                return element.html();
            } else if (expression.equalsIgnoreCase("data")) {
                return element.data();
            }
        }
        
        return element.text();
    }
}
