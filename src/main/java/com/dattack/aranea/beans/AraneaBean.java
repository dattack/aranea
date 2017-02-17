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
package com.dattack.aranea.beans;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
//import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import com.dattack.aranea.beans.web.WebBean;

/**
 * A bean that maps the root element of an Aranea configuration file.
 * 
 * @author cvarela
 * @since 0.1
 */
@XmlRootElement(name = XmlTokens.ARANEA)
public class AraneaBean {

//    @XmlElements({ //
//            @XmlElement(name = XmlTokens.WEB, type = WebBean.class), //
//            @XmlElement(name = XmlTokens.REST, type = RestBean.class)//
//    })
    @XmlElement(name = XmlTokens.WEB, type = WebBean.class, required = true)
    private final List<AbstractTaskBean> taskList;

    public AraneaBean() {
        this.taskList = new ArrayList<>();
    }

    public List<AbstractTaskBean> getTaskList() {
        return taskList;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("AraneaBean [sources=").append(taskList).append("]");
        return builder.toString();
    }
}
