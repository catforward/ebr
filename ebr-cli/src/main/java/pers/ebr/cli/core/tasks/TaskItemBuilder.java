/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pers.ebr.cli.core.tasks;

import pers.ebr.cli.core.EbrException;
import pers.ebr.cli.util.AppLogger;
import pers.ebr.cli.util.ConfigUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.FileInputStream;
import java.util.*;

import static pers.ebr.cli.core.tasks.TaskType.GROUP;
import static pers.ebr.cli.util.ConfigUtils.KEY_INSTANT_TASK;
import static pers.ebr.cli.util.MiscUtils.checkNotNull;

/**
 * <pre>
 * 从磁盘读取任务流配置文件并构建任务定义树
 * </pre>
 *
 * @author l.gong
 */
public class TaskItemBuilder {

    /** symbols in xml file */
    private static final String ATTR_ID = "id";
    private static final String ATTR_DESC = "desc";
    private static final String ATTR_COMMAND = "command";
    private static final String ATTR_DEPENDS = "depends";

    public TaskItemBuilder() {}

    /**
     * <pre>
     * build a new TaskFlow object
     * </pre>
     *
     * @return TaskFlow: the TaskFlow object
     */
    public TaskFlow load() {
        Optional<String> strVal = Optional.ofNullable(ConfigUtils.get(KEY_INSTANT_TASK));
        if (strVal.isEmpty()) {
            throw new EbrException("the path of define file is empty.");
        }
        String fullPath = strVal.get();
        if (!(fullPath.startsWith("/") || fullPath.contains(":"))) {
            fullPath =  String.format("%s/%s", System.getProperty("user.dir"), fullPath);
        }
        System.err.println(String.format("file:[%s] location:[%s]", strVal.get(), fullPath));
        try {
            //解决java.lang.AbstractMethodError:javax.xml.parsers.DocumentBuilderFactory.setFeature(Ljava/lang/String;Z)V异常
            System.setProperty("javax.xml.parsers.DocumentBuilderFactory", "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl");
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            documentBuilderFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            documentBuilderFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            documentBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            documentBuilderFactory.setXIncludeAware(false);
            documentBuilderFactory.setExpandEntityReferences(false);
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(new FileInputStream(fullPath));
            return parse(document.getDocumentElement());
        } catch (Exception ex) {
            AppLogger.dumpError(ex);
            throw new EbrException(ex);
        }
    }

    /**
     * <pre>
     * 创建一个任务节点树
     * </pre>
     *
     * @param node Task定义文件转换后的json根节点
     * @return TaskFlow
     */
    private TaskFlow parse(Node node) {
        TaskFlow flow = new TaskFlow();
        createMetaPool(null, node, flow);
        return checkNotNull(flow.build());
    }

    /**
     * <pre>
     * 创建节点树
     * </pre>
     *
     * @param parent  父节点
     * @param node    当前节点
     * @param flow    任务流
     */
    private void createMetaPool(Task parent, Node node, TaskFlow flow) {
        Task currentTask = null;
        // attributes
        if (node.hasAttributes() && Node.ELEMENT_NODE == node.getNodeType()) {
            NamedNodeMap map = node.getAttributes();
            Optional<Node> optValue = Optional.ofNullable(map.getNamedItem(ATTR_ID));
            if (optValue.isEmpty()) {
                throw new EbrException("attribute [id] must be set.");
            }
            // create a new task
            String id = optValue.get().getNodeValue().trim();
            currentTask = Optional.ofNullable(flow.getTask(id)).orElseGet(() -> new Task(id, (parent == null) ?  "": parent.id));
            // desc
            optValue = Optional.ofNullable(map.getNamedItem(ATTR_DESC));
            if (optValue.isPresent()) {
                currentTask.desc = optValue.get().getNodeValue().trim();
            }
            // command
            optValue = Optional.ofNullable(map.getNamedItem(ATTR_COMMAND));
            if (optValue.isPresent()) {
                currentTask.command = optValue.get().getNodeValue().trim();
            }
            // depends
            optValue = Optional.ofNullable(map.getNamedItem(ATTR_DEPENDS));
            if (optValue.isPresent()) {
                StringTokenizer tokenizer = new StringTokenizer(optValue.get().getNodeValue().trim(), ",", false);
                while (tokenizer.hasMoreElements()) {
                    currentTask.depends.add(tokenizer.nextToken().trim());
                }
            }
        }
        // sub elements
        NodeList nodeList = node.getChildNodes();
        int len = nodeList.getLength();
        if (currentTask != null) {
            if (len > 0) {
                currentTask.type = GROUP;
            }
            flow.addTask(currentTask);
            for (int i = 0; i < len; i++) {
                createMetaPool(currentTask, nodeList.item(i), flow);
            }
        }
    }
}
