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
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import com.dattack.aranea.beans.web.crawler.StorageBean;
import com.dattack.aranea.beans.web.crawler.StorageBean.Layout;
import com.dattack.aranea.engine.Context;
import com.dattack.aranea.util.HashUtil;
import com.dattack.jtoolbox.commons.configuration.ConfigurationUtil;

/**
 * @author cvarela
 * @since 0.1
 */
public class FilenameGenerator {

    private static final String HASH_KEY = "uri.hash";
    private static final String HASH_KEY_VARIABLE = "${" + HASH_KEY + "}";
    private static final String URL_GROUP_KEY = "url.group";
    private static final int TREE_SUBDIRECTORIES = 1;
    private static final int CHARACTERES_PER_TREE_DIRECTORY = 2;

    private final String matchedFilenamePattern;
    private final boolean computeHash4MatchedFiles;

    private final String notMatchedFilenamePattern;
    private final boolean computeHash4NotMatchedFiles;

    private final Layout layout;
    private final Pattern urlRegExPattern;

    public FilenameGenerator(final StorageBean storageBean, final Context context) {

        this.matchedFilenamePattern = storageBean.getFilenamePattern();
        this.computeHash4MatchedFiles = matchedFilenamePattern.contains(HASH_KEY_VARIABLE);

        this.notMatchedFilenamePattern = storageBean.getNotMatchedFilenamePattern();
        this.computeHash4NotMatchedFiles = notMatchedFilenamePattern.contains(HASH_KEY_VARIABLE);

        this.layout = storageBean.getLayout();

        if (StringUtils.isNotBlank(storageBean.getUrlRegEx())) {
            this.urlRegExPattern = Pattern.compile(context.interpolate(storageBean.getUrlRegEx()),
                    Pattern.CASE_INSENSITIVE);
        } else {
            this.urlRegExPattern = null;
        }
    }

    /**
     * 
     * @param uri
     * @return
     */
    public String computeFilename(final URI uri) {

        final String filename = computeFilename(uri.toString());
        if ((filename != null) && StorageBean.Layout.TREE.equals(layout)) {
            return getTreeLayout(filename);
        }
        return filename;
    }

    private String computeFilename(final String uri) {

        final BaseConfiguration configuration = new BaseConfiguration();

        String filenamePattern = matchedFilenamePattern;
        boolean computeHash = computeHash4MatchedFiles;
        
        if (urlRegExPattern != null) {

            final Matcher matcher = urlRegExPattern.matcher(uri);
            if (matcher.matches()) {
                for (int i = 0; i <= matcher.groupCount(); i++) {
                    configuration.setProperty(URL_GROUP_KEY + i, matcher.group(i));
                }
            } else if (StringUtils.isNotBlank(notMatchedFilenamePattern)) {

                filenamePattern = notMatchedFilenamePattern;
                computeHash = computeHash4NotMatchedFiles;
            } else {
                filenamePattern = null;
            }
        }

        if (computeHash && (filenamePattern != null)) {
            try {
                configuration.setProperty(HASH_KEY, HashUtil.sha1(uri));
            } catch (final NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }

        return ConfigurationUtil.interpolate(filenamePattern, configuration);
    }

    private String getTreeLayout(final String path) {

        final String filename = FilenameUtils.getName(path);
        final int index = filename.lastIndexOf(".");
        if (index <= CHARACTERES_PER_TREE_DIRECTORY * TREE_SUBDIRECTORIES) {
            // force plain layout
            return path;
        }

        final StringBuilder sb = new StringBuilder(path.length() + TREE_SUBDIRECTORIES);
        sb.append(FilenameUtils.getFullPath(path));

        String text = filename;
        for (int i = 0; i < TREE_SUBDIRECTORIES; i++) {
            sb.append(text.substring(0, CHARACTERES_PER_TREE_DIRECTORY)).append(File.separator);
            text = text.substring(CHARACTERES_PER_TREE_DIRECTORY);
        }
        sb.append(text);

        return sb.toString();
    }
}
