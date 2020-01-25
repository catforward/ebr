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
package pers.ebr.cli;

import pers.ebr.cli.core.EbrException;
import pers.ebr.cli.core.util.PathUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.*;

import static pers.ebr.cli.ConfigUtils.KEY_INSTANT_TASK;

/**
 * <pre>
 * 从磁盘读取任务流配置文件并构建任务定义树
 * </pre>
 * @author l.gong
 */
class TaskDefineFileLoader {

    private static final int INIT_CAP = 16;
    /** symbols in json file */
    private static final String ATTR_ID = "id";
    private static final String ATTR_DESC = "desc";
    private static final String ATTR_PRE_TASKS = "pre_tasks";
    private static final String ATTR_COMMAND = "command";
    /** internal symbols in app */
    private static final String KEY_ROOT_TASK = "KEY_ROOT_TASK";

    /**
     * @return true: succeeded false: failed
     */
    public TaskImpl load() {
        final String filePath = makeDefineFileFullPath();
        return loadTaskMetaFromDefineFile(filePath);
    }


    /**
     * <pre>
     * 生成一个Task定义文件的完整路径
     * </pre>
     *
     */
    private String makeDefineFileFullPath() {
        Optional<String> strVal = Optional.ofNullable(ConfigUtils.get(KEY_INSTANT_TASK));
        if (strVal.isEmpty()) {
            throw new EbrException("没有发现Task定义文件的路径");
        }
        String filePath = strVal.get();
        return filePath.startsWith("/") ? filePath : PathUtils.getDefPath() + File.separator + filePath;
    }


    /**
     * <pre>
     * 从定义文件创建一个任务节点树
     * </pre>
     *
     * @param filePath Task定义文件的完整路径
     */
    private TaskImpl loadTaskMetaFromDefineFile(String filePath) {
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
            Document document = documentBuilder.parse(filePath);
            return parse(document);
        } catch (Exception ex) {
            throw new EbrException(ex);
        }
    }

    /**
     * <pre>
     * 创建一个任务节点树
     * </pre>
     *
     * @param node Task定义文件转换后的json根节点
     * @return TaskImpl
     */
    private TaskImpl parse(Node node) {
        HashMap<String, TaskImpl> idTaskPool = new HashMap<>(INIT_CAP);
        createMetaPool(null, node, idTaskPool);
        return idTaskPool.get(KEY_ROOT_TASK);
    }

    /**
     * <pre>
     * 创建节点树
     * </pre>
     *
     * @param mParent       父节点
     * @param node     当前节点
     * @param idTaskPool 所有节点对象池
     */
    private void createMetaPool(TaskImpl mParent, Node node, Map<String, TaskImpl> idTaskPool) {
        TaskImpl currentTask = null;
        // attributes
        if (node.hasAttributes() && Node.ELEMENT_NODE == node.getNodeType()) {
            NamedNodeMap map = node.getAttributes();
            Optional<Node> optValue = Optional.ofNullable(map.getNamedItem(ATTR_ID));
            if (optValue.isEmpty()) {
                throw new EbrException("没有设置uid元素");
            }
            // 使用ID取得或创建Meta
            String id = optValue.get().getNodeValue().trim();
            currentTask = Optional.ofNullable(idTaskPool.get(id)).orElseGet(() -> {
                TaskImpl newTask = new TaskImpl(id, mParent);
                idTaskPool.put(id, newTask);
                if (newTask.parentTask() == null) {
                    idTaskPool.put(KEY_ROOT_TASK, newTask);
                } else {
                    mParent.children.add(newTask);
                }
                return newTask;
            });
            // 描述
            optValue = Optional.ofNullable(map.getNamedItem(ATTR_DESC));
            if (optValue.isPresent()) {
                currentTask.desc = optValue.get().getNodeValue().trim();
            }
            // 命令
            optValue = Optional.ofNullable(map.getNamedItem(ATTR_COMMAND));
            if (optValue.isPresent()) {
                currentTask.command = optValue.get().getNodeValue().trim();
            }
            // 触发器
            optValue = Optional.ofNullable(map.getNamedItem(ATTR_PRE_TASKS));
            if (optValue.isPresent()) {
                StringTokenizer tokenizer = new StringTokenizer(optValue.get().getNodeValue().trim(), ",", false);
                if (tokenizer.countTokens() != 0) {
                    while (tokenizer.hasMoreElements()) {
                        currentTask.preTask.add(tokenizer.nextToken().trim());
                    }
                }
            }
        }
        // 子元素
        NodeList nodeList = node.getChildNodes();
        int len = nodeList.getLength();
        if (len != 0) {
            for (int i = 0; i < len; i++) {
                createMetaPool(currentTask, nodeList.item(i), idTaskPool);
            }
        }
    }
}
