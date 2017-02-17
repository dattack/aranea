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
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.dattack.aranea.beans.web.parser.OutputBean;
import com.dattack.aranea.beans.web.parser.OutputItemBean;
import com.dattack.aranea.beans.web.parser.OutputItemGroupBean;
import com.dattack.aranea.beans.web.parser.OutputItemRefBean;
import com.dattack.aranea.beans.web.parser.OutputItemReplaceBean;
import com.dattack.aranea.beans.web.parser.OutputItemVisitor;

/**
 * @author cvarela
 * @since 0.1
 */
final class DataTransformer {

    private final OutputBean outputBean;

    public DataTransformer(final OutputBean outputBean) {
        this.outputBean = outputBean;
    }

    public List<String> execute(final Map<String, Object> attributes) {

        InnerColumnVisitor metadataColumnVisitor = new InnerColumnVisitor(attributes);

        for (OutputItemBean column : outputBean.getColumns()) {
            column.accept(metadataColumnVisitor);
        }

        return metadataColumnVisitor.getData();
    }

    private static final class InnerColumnVisitor implements OutputItemVisitor {

        private static final String EMPTY_TXT = "";

        private final Map<String, Object> map;
        private final List<String> data;

        InnerColumnVisitor(final Map<String, Object> map) {
            this.map = map;
            data = new ArrayList<String>();
        }

        @Override
        public void visite(final OutputItemGroupBean column) {
            Object baseValue = map.get(column.getRef());
            if (baseValue == null) {
                data.add(EMPTY_TXT);
            } else {
                Pattern pattern = Pattern.compile(column.getRegex());
                Matcher matcher = pattern.matcher(baseValue.toString());
                if (matcher.matches()) {
                    if (matcher.groupCount() >= column.getGroup()) {
                        data.add(matcher.group(column.getGroup()));
                    } else {
                        data.add(EMPTY_TXT);
                    }
                } else {
                    data.add(EMPTY_TXT);
                }
            }
        }

        @Override
        public void visite(final OutputItemReplaceBean column) {
            Object baseValue = map.get(column.getRef());
            if (baseValue == null) {
                data.add(EMPTY_TXT);
            } else {
                String replacement = (column.getReplacement() == null ? "" : column.getReplacement());
                data.add(baseValue.toString().replaceAll(column.getRegex(), replacement));
            }
        }

        @Override
        public void visite(final OutputItemRefBean column) {
            Object obj = map.get(column.getName());
            if (obj == null) {
                data.add(EMPTY_TXT);
            } else {
                data.add(obj.toString());
            }
        }

        public List<String> getData() {
            return data;
        }
    }
}
