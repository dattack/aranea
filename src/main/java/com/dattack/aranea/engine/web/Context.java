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
package com.dattack.aranea.engine.web;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.configuration.BaseConfiguration;

import com.dattack.jtoolbox.commons.configuration.ConfigurationUtil;

/**
 * @author cvarela
 * @since 0.1
 */
public class Context {

    public static final String CURRENT_TIMESTAMP = "current_timestamp";
    public static final String CURRENT_DATE_TIME = "current_datetime";
    
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");
    
    private final BaseConfiguration configuration;
    
    private static ThreadLocal<Context> threadLocal = new ThreadLocal<Context>(){
        @Override
        protected Context initialValue() {
            return new Context();
        }
    };
    
    public static Context get() {
        return threadLocal.get();
    }

    public Context() {
        configuration = new BaseConfiguration();
        configuration.append(ConfigurationUtil.createEnvSystemConfiguration());
        
        Date date = new Date();
        configuration.setProperty(Context.CURRENT_TIMESTAMP, date.getTime());
        configuration.setProperty(Context.CURRENT_DATE_TIME, DATE_FORMAT.format(date));
    }
    
    public void setProperty(final String key, final Object value) {
        configuration.setProperty(key, value);
    }
    
    public Object getProperty(final String key) {
        return configuration.getProperty(key);
    }
    
    public String interpolate(final Object expression) {
        return ConfigurationUtil.interpolate(expression, configuration);
    }
}
