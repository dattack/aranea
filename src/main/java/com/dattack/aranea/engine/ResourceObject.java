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
package com.dattack.aranea.engine;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

/**
 * @author cvarela
 * @since 0.1
 */
public class ResourceObject {

    private static final String ID = "_id";

    private final Map<String, Object> internalMap;

    public ResourceObject() {
        this.internalMap = new HashMap<>();
    }

    public ResourceObject(final Map<String, Object> map) {
        this.internalMap = new HashMap<>(map);
    }

    public Object put(final String key, final Object value) {
        return internalMap.put(key, value);
    }

    public Object get(final String key) {
        return internalMap.get(key);
    }

    public Object getId() {
        return get(ID);
    }

    public AbstractConfiguration compileConfiguration() {

        final BaseConfiguration configuration = new BaseConfiguration();
        configuration.setDelimiterParsingDisabled(true);

        for (final Entry<String, Object> item : internalMap.entrySet()) {
            configuration.setProperty(item.getKey(), StringUtils.trimToEmpty(ObjectUtils.toString(item.getValue())));
        }

        return configuration;
    }
}
