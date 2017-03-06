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
package com.dattack.aranea.engine.web.parser;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dattack.aranea.beans.web.WebBean;
import com.dattack.aranea.engine.Context;

/**
 * @author cvarela
 * @since 0.1
 */
public final class ParserEngine {

    private static final Logger log = LoggerFactory.getLogger(ParserEngine.class);
    
    private final Context context;
    
    public ParserEngine() {
        this.context = new Context();
    }

    public void execute(final WebBean sourceBean, final String repositoryPath) {

        log.info("Starting parser process:");
        
        String repository = context.interpolate(repositoryPath);
        log.info("Repository path: {}", repository);

        String filename = context.interpolate(sourceBean.getParser().getOutput().getDatafile());
        log.info("Output filename: {}", filename);

        final DataWriter dataWriter = new DataWriter(new File(filename));

        execute(new DataExtractor(sourceBean.getParser().getMetadata()), //
                new DataTransformer(sourceBean.getParser().getOutput()), //
                new File(repository), //
                dataWriter);
    }

    private void execute(final DataExtractor contentExtractor, final DataTransformer dataTransformer, //
            final File path, final DataWriter dataWriter) {

        if (path.isDirectory()) {
            log.debug("Directory: {}", path);
            for (File file : path.listFiles()) {
                execute(contentExtractor, dataTransformer, file, dataWriter);
            }
        } else if (path.isFile()) {
            if (!path.getName().startsWith(".")) {
                // si no es un fichero oculto ...
                executeFile(contentExtractor, dataTransformer, path, dataWriter);
            }
        }
    }

    private void executeFile(final DataExtractor contentExtractor, final DataTransformer dataTransformer,
            final File path, final DataWriter dataWriter) {

        try {
            log.debug("File {}", path);

            Document doc = Jsoup.parse(path, null);

            Map<String, Object> map = contentExtractor.execute(doc);
            map.put("path", path.getCanonicalPath());

            log.debug("Mapping from {}: {}", path, map);

            List<String> data = dataTransformer.execute(map);

            log.debug("Transformed data {}: {}", path, data);

            dataWriter.write(data);

        } catch (final Exception e) {
            log.debug("Parser error {}: {}", path, e.getMessage());
        }
    }
}
