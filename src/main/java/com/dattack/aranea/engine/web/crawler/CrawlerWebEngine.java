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

import java.lang.management.ManagementFactory;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dattack.aranea.beans.web.WebBean;

/**
 * @author cvarela
 * @since 0.1
 */
public class CrawlerWebEngine {

	private static final Logger log = LoggerFactory.getLogger(CrawlerWebEngine.class);

	public void submit(final WebBean sourceBean) {

		CrawlerWebTaskController controller = new CrawlerWebTaskController(sourceBean);
		registerMBean(controller);
		controller.execute();
	}
	
	private void registerMBean(final CrawlerWebTaskController controller) {
		try {
			MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
			String name = String.format("com.dattack.aranea.crawler:type=%s,name=%s", //
					controller.getClass().getSimpleName(), //
					controller.getSourceBean().getId());
			mbs.registerMBean(controller, new ObjectName(name));
		} catch (MBeanException | InstanceAlreadyExistsException | NotCompliantMBeanException
				| MalformedObjectNameException e) {
			log.warn("Unable to register MBean for source '{}'", controller.getSourceBean().getId(), e);
		}
	}
}
