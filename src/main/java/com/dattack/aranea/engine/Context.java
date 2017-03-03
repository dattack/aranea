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
package com.dattack.aranea.engine;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.BaseConfiguration;

import com.dattack.jtoolbox.commons.configuration.ConfigurationUtil;

/**
 * @author cvarela
 * @since 0.1
 */
public final class Context {

    public static final String CURRENT_TIMESTAMP = "current_timestamp";
    public static final String CURRENT_DATE_TIME = "current_datetime";
    public static final String CURRENT_DATE = "current_date";

    private static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");

    private final BaseConfiguration configuration;

    public Context() {
        configuration = new BaseConfiguration();
        configuration.append(ConfigurationUtil.createEnvSystemConfiguration());

        final Date date = new Date();
        configuration.setProperty(Context.CURRENT_TIMESTAMP, date.getTime());
        configuration.setProperty(Context.CURRENT_DATE_TIME, DATE_TIME_FORMAT.format(date));
        configuration.setProperty(Context.CURRENT_DATE, DATE_FORMAT.format(date));
    }

    public void setProperty(final String key, final Object value) {
        configuration.setProperty(key, value);
    }

    public AbstractConfiguration getConfiguration() {
        return (BaseConfiguration) configuration.clone();
    }

    public Object getProperty(final String key) {
        return configuration.getProperty(key);
    }

    public String interpolate(final Object expression) {
        return ConfigurationUtil.interpolate(expression, configuration);
    }
}
