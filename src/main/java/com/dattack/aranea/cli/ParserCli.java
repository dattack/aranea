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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dattack.aranea.beans.AbstractTaskBean;
import com.dattack.aranea.beans.AraneaBean;
import com.dattack.aranea.beans.jobs.Job;
import com.dattack.aranea.beans.jobs.Jobs;
import com.dattack.aranea.beans.web.WebBean;
import com.dattack.aranea.engine.web.parser.ParserEngine;
import com.dattack.aranea.util.XmlParser;

/**
 * @author cvarela
 * @since 0.1
 */
public final class ParserCli extends AbstractCli {

    private static final Logger log = LoggerFactory.getLogger(ParserCli.class);

    private static final Option REPOSITORY_OPTION = Option.builder("r").longOpt("repo").hasArg().argName("directory")
            .desc("the repository path").required().build();

    private static final Options OPTIONS = new Options() //
            .addOption(FILE_OPTION) //
            .addOption(JOB_OPTION) //
            .addOption(TASK_OPTION) //
            .addOption(REPOSITORY_OPTION);

    private static void execute(final String xmlConfigurationFilename, final String sourceName,
            final String jobsFilename, final String repositoryPath) throws JAXBException {

        Jobs jobs = getJobs(jobsFilename);

        AraneaBean araneaBean = (AraneaBean) XmlParser.parse(AraneaBean.class, xmlConfigurationFilename);

        for (AbstractTaskBean sourceBean : araneaBean.getTaskList()) {
            if (matches(sourceBean, sourceName) && (sourceBean instanceof WebBean)) {
                executeTask((WebBean) sourceBean, repositoryPath, jobs);
            }
        }
    }

    private static void executeTask(WebBean webBean, final String repositoryPath, Jobs jobs) {

        log.info("Starting source '{}'", webBean.getId());

        if (jobs == null) {
            new ParserEngine(webBean, repositoryPath).execute();
        } else {
            for (Job job : jobs.getJobList()) {
                new ParserEngine(webBean, repositoryPath, job).execute();
            }
        }
    }

    public static void main(final String[] args) {

        try {

            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(OPTIONS, args);

            final String configurationFilename = cmd.getOptionValue(FILE_OPTION.getOpt());
            final String repository = cmd.getOptionValue(REPOSITORY_OPTION.getOpt());
            String sourceName = null;
            String jobsFilename = null;

            if (cmd.hasOption(TASK_OPTION.getOpt())) {
                sourceName = cmd.getOptionValue(TASK_OPTION.getOpt());
            }

            if (cmd.hasOption(JOB_OPTION.getOpt())) {
                jobsFilename = cmd.getOptionValue(JOB_OPTION.getOpt());
            }

            execute(configurationFilename, sourceName, jobsFilename, repository);

        } catch (final ParseException e) {
            showUsage("parser", OPTIONS, "Welcome to the Web parser client\n");
        } catch (final Throwable e) {
            log.error(e.getMessage(), e);
        }
    }

    private ParserCli() {
        // Main class
    }
}
