/*
 * Copyright (c) 2015, The Dattack team (http://www.dattack.com)
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dattack.aranea.cli;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dattack.aranea.beans.AbstractTaskBean;
import com.dattack.aranea.beans.AraneaBean;
import com.dattack.aranea.beans.web.WebBean;
import com.dattack.aranea.engine.web.parser.ParserEngine;
import com.dattack.aranea.util.CommandLine;
import com.dattack.aranea.util.XmlParser;

/**
 * @author cvarela
 * @since 0.1
 */
public final class ParserClient {

    private static final Logger log = LoggerFactory.getLogger(ParserClient.class);

    public static void main(final String[] args) {
        try {

            CommandLine commandLine = new CommandLine(args);
            final String configurationFilename = commandLine.nextArg();
            final String sourceName = commandLine.nextArg();

            if (StringUtils.isBlank(configurationFilename)) {
                System.err.println("Usage: ParserClient <configuration_file> [<source_name>]");
            } else {
                execute(configurationFilename, sourceName);
            }

        } catch (final Throwable e) {
            log.error(e.getMessage());
        }
    }

    private ParserClient() {
        // Main class
    }

    private static void execute(final String xmlConfigurationFilename, final String sourceName) throws JAXBException {

        AraneaBean araneaBean = (AraneaBean) XmlParser.parse(AraneaBean.class, xmlConfigurationFilename);

        ParserEngine parserEngine = new ParserEngine();
        for (AbstractTaskBean sourceBean : araneaBean.getTaskList()) {

            if (sourceBean instanceof WebBean) {
                if ((sourceName != null) && !sourceName.equalsIgnoreCase(sourceBean.getId())) {
                    continue;
                }
                log.info(sourceBean.toString());
                parserEngine.execute((WebBean) sourceBean);
            }
        }
    }
}
