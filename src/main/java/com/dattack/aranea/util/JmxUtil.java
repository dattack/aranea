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
package com.dattack.aranea.util;

import java.lang.management.ManagementFactory;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author cvarela
 * @since 0.1
 */
public final class JmxUtil {

    private static final Logger log = LoggerFactory.getLogger(JmxUtil.class);

    private JmxUtil() {
        // utility class
    }
    
    public static ObjectName createObjectName(final String name) {

        if (name == null) {
            return null;
        }

        try {
            return new ObjectName(name);
        } catch (MalformedObjectNameException e) {
            log.warn(e.getMessage());
            return null;
        }
    }

    public static ObjectInstance registerMBean(final Object obj, final ObjectName objectName) {
        try {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            return mbs.registerMBean(obj, objectName);
        } catch (MBeanException | InstanceAlreadyExistsException | NotCompliantMBeanException e) {
            log.warn("Unable to register MBean with name '{}'", ObjectUtils.toString(objectName), e);
        }
        return null;
    }
}
