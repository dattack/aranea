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
package com.dattack.aranea.beans.web.parser;

import javax.xml.bind.annotation.XmlElement;

import com.dattack.aranea.beans.XmlTokens;

/**
 * @author cvarela
 * @since 0.1
 */
public class ParserBean {

    @XmlElement(name = XmlTokens.METADATA, required = true)
    private MetadataBean metadata;

    @XmlElement(name = XmlTokens.OUTPUT, required = true)
    private OutputBean output;

    public MetadataBean getMetadata() {
        return metadata;
    }

    public OutputBean getOutput() {
        return output;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ParserBean [metadata=").append(metadata).append(", output=").append(output).append("]");
        return builder.toString();
    }
}
