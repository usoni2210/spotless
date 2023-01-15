/*
 * Copyright 2023 DiffPlug
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
package com.diffplug.spotless.maven.yaml;

import java.util.Collections;
import java.util.Map;

import com.diffplug.spotless.maven.FormatterFactory;

import org.apache.maven.plugins.annotations.Parameter;

import com.diffplug.spotless.FormatterStep;
import com.diffplug.spotless.json.JacksonConfig;
import com.diffplug.spotless.maven.FormatterStepConfig;
import com.diffplug.spotless.maven.FormatterStepFactory;
import com.diffplug.spotless.yaml.JacksonYamlStep;

/**
 * A {@link FormatterFactory} implementation that corresponds to {@code <jackson>...</jackson>} configuration element.
 */
public class JacksonYaml implements FormatterStepFactory {

	@Parameter
	private String version = JacksonYamlStep.defaultVersion();

	@Parameter
	boolean endWithEol = new JacksonConfig().isEndWithEol();

	@Parameter
	boolean spaceBeforeSeparator = new JacksonConfig().isSpaceBeforeSeparator();

	@Parameter
	private Map<String, Boolean> features = Collections.emptyMap();

	@Override
	public FormatterStep newFormatterStep(FormatterStepConfig stepConfig) {
		JacksonConfig jacksonConfig = new JacksonConfig();

		if (features != null) {
			jacksonConfig.appendFeatureToToggle(features);
		}
		jacksonConfig.setEndWithEol(endWithEol);

		return JacksonYamlStep
				.create(jacksonConfig, version, stepConfig.getProvisioner());
	}
}
