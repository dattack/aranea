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

/**
 * @author cvarela
 * @since 0.1
 */
public class CommandLine {

	private final String[] args;
	private int index;

	public CommandLine(final String[] args) {
		if (args == null) {
			throw new NullPointerException("The 'args' parameter can't be null");
		}

		this.args = args;
		this.index = 0;
	}
	
	public String nextArg() {
		return getValue(index++);
	}

	public String getValue(final int index) {

		return getValue(index, null);
	}
	
	public String getValue(final int index, final String defaultValue) {

		if (args.length > index) {
			return args[index];
		}
		return defaultValue;
	}
}
