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

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dattack.aranea.beans.AbstractTaskBean;
import com.dattack.aranea.beans.AraneaBean;
import com.dattack.aranea.beans.jobs.Job;
import com.dattack.aranea.beans.jobs.Jobs;
import com.dattack.aranea.beans.rest.RestBean;
import com.dattack.aranea.engine.rest.CrawlerRestTaskController;
import com.dattack.aranea.util.CommandLine;
import com.dattack.aranea.util.XmlParser;

/**
 * The main client used to execute the crawler bot.
 * 
 * @author cvarela
 * @since 0.1
 */
public final class RestClient {

    private static final Logger log = LoggerFactory.getLogger(RestClient.class);

    private static AbstractTaskBean getTask(final AraneaBean araneaBean, final String sourceName) throws Exception {

        for (AbstractTaskBean sourceBean : araneaBean.getTaskList()) {

            if (sourceName == null || sourceName.equalsIgnoreCase(sourceBean.getId())) {
                return sourceBean;
            }
        }

        return null;
    }

    private static void execute(final String xmlConfigurationFilename, final String sourceName,
            final String jobsFilename) throws Exception {

        AraneaBean araneaBean = (AraneaBean) XmlParser.parse(AraneaBean.class, xmlConfigurationFilename);
        AbstractTaskBean task = getTask(araneaBean, sourceName);

        if (task instanceof RestBean) {

            RestBean restBean = (RestBean) task;
            Jobs jobs = CliHelper.getJobs(jobsFilename);

            if (jobs == null) {
                new CrawlerRestTaskController(restBean, null).execute();
            } else {
                for (Job job : jobs.getJobList()) {
                    new CrawlerRestTaskController(restBean, job).execute();
                }
            }
        }
    }

    public static void main(final String[] args) {

        try {

            CommandLine commandLine = new CommandLine(args);
            final String configurationFilename = commandLine.nextArg();
            final String sourceName = commandLine.nextArg();
            final String jobsFilename = commandLine.nextArg();

            if (StringUtils.isBlank(configurationFilename) || StringUtils.isBlank(sourceName)) {
                System.err.println("Usage: RestClient <configuration_file> <source_name> [<jobs_file>]");
            } else {
                execute(configurationFilename, sourceName, jobsFilename);
            }

        } catch (final Throwable e) {
            log.error(e.getMessage(), e);
        }
    }

    private RestClient() {
        // Main class
    }
}
