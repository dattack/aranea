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
package com.dattack.aranea.cli;

import javax.xml.bind.JAXBException;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.lang.StringUtils;

import com.dattack.aranea.beans.AbstractTaskBean;
import com.dattack.aranea.beans.jobs.Jobs;
import com.dattack.aranea.util.XmlParser;

/**
 * @author cvarela
 * @since 0.1
 */
public abstract class AbstractCli {
    
    protected static final Option FILE_OPTION = Option.builder("f").longOpt("file").hasArg().argName("filename")
            .desc("the aranea configuration file").required().build();

    protected static final Option TASK_OPTION = Option.builder("t").longOpt("task").hasArg().argName("taskname")
            .desc("the task name to execute").build();

    protected static final Option JOB_OPTION = Option.builder("j").longOpt("jobs").hasArg().argName("filename")
            .desc("custom job configuration").build();

    protected static Jobs getJobs(final String jobsFilename) throws JAXBException {
        if (StringUtils.isBlank(jobsFilename)) {
            return null;
        }
        return (Jobs) XmlParser.parse(Jobs.class, jobsFilename);
    }
    
    protected static void showUsage(final String cmdLineSyntax, final Options options, final String welcome) {

        final int leftPadding = 8;
        final String header = "\nOptions:";
        final String footer = "\nPlease report issues at https://github.com/dattack/aranea/issues";

        HelpFormatter formatter = new HelpFormatter();
        formatter.setLeftPadding(leftPadding);

        System.out.println(welcome);
        formatter.printHelp(cmdLineSyntax, header, options, footer, true);
    }
    
    protected static boolean matches(final AbstractTaskBean bean, final String sourceName) {
        return StringUtils.isBlank(sourceName) || StringUtils.equalsIgnoreCase(sourceName, bean.getId());
    }
}
