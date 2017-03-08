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
package com.dattack.aranea.engine.web.crawler;

import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.dattack.aranea.beans.web.crawler.URINormalizerBean;

/**
 * @author cvarela
 * @since 0.1
 */
class LinkNormalizer {

    private Pattern pattern;
    private String replacement;

    public LinkNormalizer(final URINormalizerBean uriNormalizerBean) {
        this.pattern = Pattern.compile(uriNormalizerBean.getRegex());
        this.replacement = StringUtils.defaultString(uriNormalizerBean.getReplacement(), "");
    }

    public String normalize(final String originalLink) {
        return pattern.matcher(originalLink).replaceAll(replacement);
    }
}
