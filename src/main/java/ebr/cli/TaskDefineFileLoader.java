/*
  MIT License

  Copyright (c) 2019 catforward

  Permission is hereby granted, free of charge, to any person obtaining a copy
  of this software and associated documentation files (the "Software"), to deal
  in the Software without restriction, including without limitation the rights
  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  copies of the Software, and to permit persons to whom the Software is
  furnished to do so, subject to the following conditions:

  The above copyright notice and this permission notice shall be included in all
  copies or substantial portions of the Software.

  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  SOFTWARE.

 */
package ebr.cli;

import ebr.core.util.PathUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.*;

import static ebr.cli.ConfigUtils.Item.KEY_INSTANT_TASK;

/**
 * <pre>
 * 从磁盘读取任务流配置文件并构建任务定义树
 * </pre>
 * @author catforward
 */
class TaskDefineFileLoader {

    private final static int INIT_CAP = 16;
    /** symbols in json file */
    private final static String ATTR_ID = "id";
    private final static String ATTR_DESC = "desc";
    private final static String ATTR_PRE_TASKS = "pre_tasks";
    private final static String ATTR_COMMAND = "command";
    /** internal symbols in app */
    private final static String KEY_ROOT_TASK = "KEY_ROOT_TASK";

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
            throw new RuntimeException("没有发现Task定义文件的路径");
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
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = documentBuilder.parse(filePath);
            return parse(document);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
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
                throw new RuntimeException("没有设置uid元素");
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
