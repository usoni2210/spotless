/*
 * Copyright 2021-2023 DiffPlug
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
package com.diffplug.spotless.glue.json;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.diffplug.spotless.FormatterFunc;
import com.diffplug.spotless.json.JacksonConfig;

/**
 * A {@link FormatterFunc} based on Jackson library
 */
// https://github.com/FasterXML/jackson-dataformats-text/issues/372
public class JsonJacksonFormatterFunc implements FormatterFunc {
	private JacksonConfig jacksonConfig;

	public JsonJacksonFormatterFunc(JacksonConfig jacksonConfig) {
		this.jacksonConfig = jacksonConfig;
	}

	@Override
	public String apply(String input) throws Exception {
		ObjectMapper objectMapper = makeObjectMapper();

		return format(objectMapper, input);
	}

	/**
	 * @return a {@link JsonFactory}. May be overridden to handle alternative formats.
	 * @see <a href="https://github.com/FasterXML/jackson-dataformats-text">jackson-dataformats-text</a>
	 */
	protected JsonFactory makeJsonFactory() {
		return new JsonFactory();
	}

	protected ObjectMapper makeObjectMapper() {
		JsonFactory jsonFactory = makeJsonFactory();
		ObjectMapper objectMapper = new ObjectMapper(jsonFactory);

		// Configure the ObjectMapper
		// https://github.com/FasterXML/jackson-databind#commonly-used-features
		jacksonConfig.getFeatureToToggle().forEach((rawFeature, toggle) -> {
			// https://stackoverflow.com/questions/3735927/java-instantiating-an-enum-using-reflection
			SerializationFeature feature = SerializationFeature.valueOf(rawFeature);

			objectMapper.configure(feature, toggle);
		});

		return objectMapper;
	}

	protected String format(ObjectMapper objectMapper, String input) throws IllegalArgumentException, IOException {
		// We may consider adding manually an initial '---' prefix to help management of multiple documents
		// if (!input.trim().startsWith("---")) {
		// 	input = "---" + "\n" + input;
		// }

		try {
			// https://stackoverflow.com/questions/25222327/deserialize-pojos-from-multiple-yaml-documents-in-a-single-file-in-jackson
			// https://github.com/FasterXML/jackson-dataformats-text/issues/66#issuecomment-375328648
			// 2023-01: For now, we get 'Cannot deserialize value of type `com.fasterxml.jackson.databind.node.ObjectNode` from Array value'
			//			JsonParser yamlParser = objectMapper.getFactory().createParser(input);
			//			List<ObjectNode> docs = objectMapper.readValues(yamlParser, ObjectNode.class).readAll();
			//			return objectMapper.writeValueAsString(docs);

			// 2023-01: This returns JSON instead of YAML
			// This will transit with a JsonNode
			// A JsonNode may keep the comments from the input node
			// JsonNode jsonNode = objectMapper.readTree(input);
			//Not 'toPrettyString' as one could require no INDENT_OUTPUT
			// return jsonNode.toPrettyString();
			ObjectNode objectNode = objectMapper.readValue(input, ObjectNode.class);
			String outputFromjackson = objectMapper.writeValueAsString(objectNode);

			if (jacksonConfig.isEndWithEol() && !outputFromjackson.endsWith("\n")) {
				outputFromjackson += "\n";
			}

			return outputFromjackson;
		} catch (JsonProcessingException e) {
			throw new AssertionError("Unable to format. input='" + input + "'", e);
		}
	}
}
