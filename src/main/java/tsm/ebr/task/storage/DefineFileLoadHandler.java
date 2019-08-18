/**
 * MIT License
 *
 * Copyright (c) 2019 catforward
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package tsm.ebr.task.storage;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import tsm.ebr.base.Const;
import tsm.ebr.base.Handler.HandlerContext;
import tsm.ebr.base.Handler.IHandler;
import tsm.ebr.base.Message.Symbols;
import tsm.ebr.base.Task.Type;
import tsm.ebr.base.Task.Unit;
import tsm.ebr.util.ConfigUtils;
import tsm.ebr.util.PathUtils;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.*;
import java.util.logging.Logger;

import static tsm.ebr.base.Message.Symbols.MSG_DATA_TASK_ROOT_UNIT;
import static tsm.ebr.base.Task.Symbols.*;
import static tsm.ebr.util.ConfigUtils.Item.KEY_INSTANT_TASK;

/**
 * <pre>
 * 从磁盘读取任务流配置文件并构建任务定义树
 * </pre>
 * @author catforward
 */
public class DefineFileLoadHandler implements IHandler {
    private final static Logger logger = Logger.getLogger(DefineFileLoadHandler.class.getCanonicalName());

    public DefineFileLoadHandler() {}

    /**
     * @param context 上下文
     * @return true: succeeded false: failed
     */
    @Override
    public boolean doHandle(HandlerContext context) {
        String filePath = makeDefineFileFullPath(context);
        Unit rootUnit = loadTaskMetaFromDefineFile(filePath);
        context.setNextAction(Symbols.MSG_ACT_TASK_META_CREATED);
        context.addHandlerResult(MSG_DATA_TASK_ROOT_UNIT, rootUnit);
        return true;
    }


    /**
     * <pre>
     * 生成一个Task定义文件的完整路径
     * </pre>
     *
     * @param context
     */
    private String makeDefineFileFullPath(HandlerContext context) {
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
    private Unit loadTaskMetaFromDefineFile(String filePath) {
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
     * @return Node
     */
    private Unit parse(Node node) {
        HashMap<String, Unit> uidUnitPool = new HashMap<>(Const.INIT_CAP);
        HashMap<String, List<String>> urlPredecessorIdMap = new HashMap<>(Const.INIT_CAP);
        createMetaPool(null, node, uidUnitPool, urlPredecessorIdMap);
        Unit rootUnit = uidUnitPool.get(KEY_ROOT_UNIT);
        updatePredecessors(rootUnit, uidUnitPool, urlPredecessorIdMap);
        return rootUnit;
    }

    /**
     * <pre>
     * 创建节点树
     * </pre>
     *
     * @param mParent       父节点
     * @param node     当前节点
     * @param uidUnitPool 所有节点对象池
     */
    private void createMetaPool(Unit mParent, Node node,
                                Map<String, Unit> uidUnitPool,
                                Map<String, List<String>> urlPredecessorIdMap) {
        Unit currentUnit = null;
        // attributes
        if (node.hasAttributes() && Node.ELEMENT_NODE == node.getNodeType()) {
            NamedNodeMap map = node.getAttributes();
            Optional<Node> optValue = Optional.ofNullable(map.getNamedItem(ATTR_ID));
            if (optValue.isEmpty()) {
                throw new RuntimeException("没有设置uid元素");
            }
            // 使用ID取得或创建Meta
            String uid = optValue.get().getNodeValue().trim();
            currentUnit = Optional.ofNullable(uidUnitPool.get(uid)).orElseGet(() -> {
                Unit newUnit = new Unit(uid, mParent);
                uidUnitPool.put(uid, newUnit);
                if (newUnit.parent == null) {
                    newUnit.url = String.format("/%s", uid);
                    newUnit.type = Type.ROOT;
                    uidUnitPool.put(KEY_ROOT_UNIT, newUnit);
                } else {
                    newUnit.url = String.format("%s/%s", mParent.url, uid);
                    mParent.children.add(newUnit);
                }
                return newUnit;
            });
            // 描述
            optValue = Optional.ofNullable(map.getNamedItem(ATTR_DESC));
            if (optValue.isPresent()) {
                currentUnit.desc = optValue.get().getNodeValue().trim();
            }
            // 命令
            optValue = Optional.ofNullable(map.getNamedItem(ATTR_COMMAND));
            if (optValue.isPresent()) {
                currentUnit.command = optValue.get().getNodeValue().trim();
            }
            // 触发器
            optValue = Optional.ofNullable(map.getNamedItem(ATTR_PREDECESSORS));
            if (optValue.isPresent()) {
                StringTokenizer tokenizer = new StringTokenizer(optValue.get().getNodeValue().trim(), ",", false);
                if (tokenizer.countTokens() != 0) {
                    List<String> pIds = urlPredecessorIdMap.get(currentUnit.url);
                    if (pIds == null) {
                        pIds = new ArrayList<>(Const.INIT_CAP);
                        urlPredecessorIdMap.put(currentUnit.url, pIds);
                    }
                    while (tokenizer.hasMoreElements()) {
                        pIds.add(tokenizer.nextToken().trim());
                    }
                }
            }
        }
        // 子元素
        NodeList nodeList = node.getChildNodes();
        int len = nodeList.getLength();
        if (len != 0) {
            for (int i = 0; i < len; i++) {
                createMetaPool(currentUnit, nodeList.item(i), uidUnitPool, urlPredecessorIdMap);
            }
            if (currentUnit != null && Type.ROOT != currentUnit.type) {
                currentUnit.type = Type.MODULE;
            }
        } else {
            if (currentUnit != null && Type.ROOT != currentUnit.type) {
                currentUnit.type = Type.TASK;
            }
        }
    }

    /**
     * <pre>
     * 更新任务单元间的前驱条件关系
     * </pre>
     *
     * @param unit
     * @param uidUnitPool
     * @param urlPredecessorIdMap
     */
    private void updatePredecessors(Unit unit,
                                    Map<String, Unit> uidUnitPool,
                                    Map<String, List<String>> urlPredecessorIdMap) {
        List<String> pIds = urlPredecessorIdMap.get(unit.url);
        if (pIds != null && !pIds.isEmpty()) {
            pIds.forEach(id -> {
                Unit pUnit = uidUnitPool.get(id);
                if (pUnit != null) {
                    unit.preconditions.add(pUnit);
                }
            });
        }
        for(Unit child : unit.children) {
            updatePredecessors(child, uidUnitPool, urlPredecessorIdMap);
        }
    }
}
