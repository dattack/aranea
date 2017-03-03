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

import java.io.File;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.PropertyConverter;
import org.apache.commons.lang.StringUtils;

import com.dattack.aranea.beans.web.crawler.StorageBean;
import com.dattack.aranea.engine.Context;
import com.dattack.aranea.util.HashUtil;

/**
 * @author cvarela
 * @since 0.1
 */
public class FilenameGenerator {

    private static final String HASH_KEY = "uri.hash";
    private static final String HASH_KEY_VARIABLE = "${" + HASH_KEY + "}";
    private static final String URL_GROUP_KEY = "url.group";
    private static final int TREE_SUBDIRECTORIES = 1;

    private final StorageBean storageBean;
    private Pattern urlRegExPattern;

    public FilenameGenerator(final StorageBean storageBean, final Context context) {

        this.storageBean = storageBean;

        if (StringUtils.isNotBlank(storageBean.getUrlRegEx())) {
            this.urlRegExPattern = Pattern.compile(context.interpolate(storageBean.getUrlRegEx()));
        }
    }

    public String getFilename(final URI uri) {

        final String hash = getHashFilename(uri);
        if (hash != null) {
            if (StorageBean.TREE_LAYOUT.equalsIgnoreCase(StringUtils.trimToEmpty(storageBean.getLayout()))) {
                return getHashTreePath(hash);
            }
        }
        return hash;
    }

    private String getHashFilename(final URI uri) {

        final BaseConfiguration configuration = new BaseConfiguration();

        final String uriAsText = uri.toString();

        String filenamePattern = storageBean.getFilenamePattern();
        if (urlRegExPattern != null) {

            final Matcher matcher = urlRegExPattern.matcher(uriAsText);

            if (matcher.matches()) {
                for (int i = 0; i <= matcher.groupCount(); i++) {
                    configuration.setProperty(URL_GROUP_KEY + i, matcher.group(i));
                }
            } else {
                if (!storageBean.isStoreNotMatching()) {
                    // not store this file
                    return null;
                }

                if (StringUtils.isNotBlank(storageBean.getFilenameNotMatchingPattern())) {
                    filenamePattern = storageBean.getFilenameNotMatchingPattern();
                }
            }
        }

        if (filenamePattern.contains(HASH_KEY_VARIABLE)) {
            try {
                configuration.setProperty(HASH_KEY, HashUtil.md5(uriAsText));
            } catch (final NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }

        return PropertyConverter.interpolate(filenamePattern, configuration).toString();
    }

    private String getHashTreePath(final String hash) {

        final int index = hash.lastIndexOf(".");
        if (index <= 2 * TREE_SUBDIRECTORIES) {
            // force plain layout
            return hash;
        }

        final StringBuilder sb = new StringBuilder();

        String text = hash;
        for (int i = 0; i < TREE_SUBDIRECTORIES; i++) {
            sb.append(text.substring(0, 2)).append(File.separator);
            text = text.substring(2);
        }
        sb.append(text);

        return sb.toString();
    }
}
