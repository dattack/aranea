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

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.io.FileUtils;

/**
 * @author cvarela
 * @since 0.1
 */
class DataWriter {

    private static final String EOL = System.getProperty("line.separator");
    private static final String SEPARATOR = ",";
    private static final String QUOTE = "\"";

    private final File datafile;

    public DataWriter(final File datafile) {
        this.datafile = datafile;
    }

    synchronized void write(final List<String> data) throws IOException {

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < data.size(); i++) {
            if (i > 0) {
                sb.append(SEPARATOR);
            }
            if (data.get(i) != null) {
                sb.append(QUOTE).append(data.get(i)).append(QUOTE);
            }
        }
        sb.append(EOL);

        FileUtils.writeStringToFile(datafile, sb.toString(), StandardCharsets.UTF_8, true);
    }
}
