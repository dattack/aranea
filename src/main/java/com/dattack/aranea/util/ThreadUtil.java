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
package com.dattack.aranea.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author cvarela
 * @since 0.1
 */
public final class ThreadUtil {

	private static final Logger log = LoggerFactory.getLogger(ThreadUtil.class);
	
	private ThreadUtil() {
		// static class
	}

	public static void sleep(final long timeout) {

		synchronized (Thread.currentThread()) {
			try {
				if (timeout > 0) {
					Thread.currentThread().wait(timeout);
				}
			} catch (final InterruptedException e) {
				log.info("Thread interrupted: {}", e.getMessage());
			}
		}
	}
}
